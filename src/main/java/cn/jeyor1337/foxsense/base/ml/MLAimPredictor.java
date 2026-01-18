package cn.jeyor1337.foxsense.base.ml;

import java.io.File;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import net.minecraft.client.MinecraftClient;

public class MLAimPredictor {

    private static MLAimPredictor instance;

    private OrtEnvironment env;
    private OrtSession session;
    private boolean initialized = false;

    private float[] inputMean;
    private float[] inputScale;
    private boolean useNormalization = false;

    private static final int INPUT_DIM = 6;
    private static final int OUTPUT_DIM = 2;

    private MLAimPredictor() {
    }

    public static MLAimPredictor getInstance() {
        if (instance == null) {
            instance = new MLAimPredictor();
        }
        return instance;
    }

    public boolean initialize() {
        if (initialized) {
            return true;
        }

        try {
            File modelDir = new File(MinecraftClient.getInstance().runDirectory, "foxsense/ml");
            File modelFile = new File(modelDir, "aim_model.onnx");

            if (!modelFile.exists()) {
                System.out.println("[MLAimPredictor] Model file not found: " + modelFile.getAbsolutePath());
                return false;
            }

            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

            session = env.createSession(modelFile.getAbsolutePath(), opts);

            loadScalerInfo(modelDir);

            initialized = true;
            System.out.println("[MLAimPredictor] Model loaded successfully");
            return true;

        } catch (Exception e) {
            System.err.println("[MLAimPredictor] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadScalerInfo(File modelDir) {
        File scalerFile = new File(modelDir, "scaler_info.json");
        if (!scalerFile.exists()) {
            System.out.println("[MLAimPredictor] No scaler info found, using raw features");
            return;
        }

        try (FileReader reader = new FileReader(scalerFile)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            var meanArray = json.getAsJsonArray("mean");
            var scaleArray = json.getAsJsonArray("scale");

            inputMean = new float[INPUT_DIM];
            inputScale = new float[INPUT_DIM];

            for (int i = 0; i < INPUT_DIM; i++) {
                inputMean[i] = meanArray.get(i).getAsFloat();
                inputScale[i] = scaleArray.get(i).getAsFloat();
            }

            useNormalization = true;
            System.out.println("[MLAimPredictor] Scaler info loaded");

        } catch (Exception e) {
            System.err.println("[MLAimPredictor] Failed to load scaler: " + e.getMessage());
        }
    }

    public AimOutput predict(AimFeatures features) {
        if (!initialized) {
            return new AimOutput(0, 0);
        }

        try {
            float[] input = features.toArray();

            if (useNormalization) {
                input = normalize(input);
            }

            FloatBuffer inputBuffer = FloatBuffer.wrap(input);
            long[] shape = new long[] { 1, INPUT_DIM };

            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputBuffer, shape);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input", inputTensor);

            try (OrtSession.Result result = session.run(inputs)) {
                float[][] output = (float[][]) result.get(0).getValue();
                inputTensor.close();

                return new AimOutput(output[0][0], output[0][1]);
            }

        } catch (Exception e) {
            System.err.println("[MLAimPredictor] Prediction failed: " + e.getMessage());
            return new AimOutput(0, 0);
        }
    }

    private float[] normalize(float[] input) {
        float[] normalized = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            normalized[i] = (input[i] - inputMean[i]) / inputScale[i];
        }
        return normalized;
    }

    public void shutdown() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (env != null) {
            try {
                env.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
