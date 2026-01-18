import os

DATA_DIR = os.path.join(os.path.dirname(__file__), "data")
MODEL_DIR = os.path.join(os.path.dirname(__file__), "models")
ONNX_OUTPUT_PATH = os.path.join(MODEL_DIR, "aim_model.onnx")

INPUT_FEATURES = [
    "deltaYaw",
    "deltaPitch", 
    "deltaX",
    "deltaY",
    "deltaZ",
    "distance",
]

OUTPUT_FEATURES = [
    "outputDeltaYaw",
    "outputDeltaPitch"
]

INPUT_DIM = len(INPUT_FEATURES)
OUTPUT_DIM = len(OUTPUT_FEATURES)

HIDDEN_DIMS = [64, 32]
DROPOUT_RATE = 0.1

BATCH_SIZE = 64
LEARNING_RATE = 0.0015
EPOCHS = 10000
WEIGHT_DECAY = 1e-5

TRAIN_SPLIT = 0.8
VALIDATION_SPLIT = 0.1

NORMALIZE_INPUT = True
NORMALIZE_OUTPUT = False

EARLY_STOPPING_PATIENCE = 60
LR_SCHEDULER_PATIENCE = 30
LR_SCHEDULER_FACTOR = 0.6

RANDOM_SEED = 42
