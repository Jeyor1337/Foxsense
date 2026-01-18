import os
import json
import pickle
import torch
import onnx
import onnxruntime as ort
import numpy as np

import config
from model import create_model


def export_to_onnx(model_path, output_path=None, model_type='simple'):
    if output_path is None:
        output_path = config.ONNX_OUTPUT_PATH
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    model = create_model(model_type)
    checkpoint = torch.load(model_path, map_location='cpu')
    model.load_state_dict(checkpoint['model_state_dict'])
    model.eval()
    
    dummy_input = torch.randn(1, config.INPUT_DIM)
    
    torch.onnx.export(
        model,
        dummy_input,
        output_path,
        export_params=True,
        opset_version=11,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={
            'input': {0: 'batch_size'},
            'output': {0: 'batch_size'}
        }
    )
    
    print(f"ONNX model exported to: {output_path}")
    
    onnx_model = onnx.load(output_path)
    onnx.checker.check_model(onnx_model)
    print("ONNX model validation passed!")
    
    return output_path


def verify_onnx_model(onnx_path, pytorch_model_path, model_type='simple'):
    model = create_model(model_type)
    checkpoint = torch.load(pytorch_model_path, map_location='cpu')
    model.load_state_dict(checkpoint['model_state_dict'])
    model.eval()
    
    ort_session = ort.InferenceSession(onnx_path)
    
    test_input = np.random.randn(10, config.INPUT_DIM).astype(np.float32)
    
    with torch.no_grad():
        pytorch_output = model(torch.from_numpy(test_input)).numpy()
    
    ort_inputs = {ort_session.get_inputs()[0].name: test_input}
    onnx_output = ort_session.run(None, ort_inputs)[0]
    
    max_diff = np.max(np.abs(pytorch_output - onnx_output))
    mean_diff = np.mean(np.abs(pytorch_output - onnx_output))
    
    print(f"Max difference: {max_diff}")
    print(f"Mean difference: {mean_diff}")
    
    if max_diff < 1e-5:
        print("Verification passed! ONNX model matches PyTorch model.")
        return True
    else:
        print("Warning: Differences detected between PyTorch and ONNX outputs.")
        return False


def export_scaler_info(scaler_path, output_path):
    with open(scaler_path, 'rb') as f:
        scaler = pickle.load(f)
    
    scaler_info = {
        'mean': scaler.mean_.tolist(),
        'scale': scaler.scale_.tolist(),
        'feature_names': config.INPUT_FEATURES
    }
    
    with open(output_path, 'w') as f:
        json.dump(scaler_info, f, indent=2)
    
    print(f"Scaler info exported to: {output_path}")
    return output_path


def export_full_model(model_type='simple'):
    model_path = os.path.join(config.MODEL_DIR, f'best_model_{model_type}.pt')
    scaler_path = os.path.join(config.MODEL_DIR, f'best_model_{model_type}_scaler.pkl')
    
    if not os.path.exists(model_path):
        print(f"Model not found: {model_path}")
        print("Please train a model first using train.py")
        return None
    
    onnx_path = export_to_onnx(model_path, model_type=model_type)
    
    verify_onnx_model(onnx_path, model_path, model_type)
    
    if os.path.exists(scaler_path):
        scaler_info_path = os.path.join(config.MODEL_DIR, 'scaler_info.json')
        export_scaler_info(scaler_path, scaler_info_path)
    
    model_info = {
        'model_type': model_type,
        'input_dim': config.INPUT_DIM,
        'output_dim': config.OUTPUT_DIM,
        'input_features': config.INPUT_FEATURES,
        'output_features': config.OUTPUT_FEATURES,
        'hidden_dims': config.HIDDEN_DIMS,
        'onnx_path': os.path.basename(onnx_path)
    }
    
    info_path = os.path.join(config.MODEL_DIR, 'model_info.json')
    with open(info_path, 'w') as f:
        json.dump(model_info, f, indent=2)
    
    print(f"\nExport complete!")
    print(f"ONNX model: {onnx_path}")
    print(f"Model info: {info_path}")
    
    return onnx_path


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='Export trained model to ONNX')
    parser.add_argument('--model', type=str, default='simple',
                        choices=['simple', 'residual', 'lstm'],
                        help='Model type to export')
    parser.add_argument('--input', type=str, default=None,
                        help='Path to PyTorch model file')
    parser.add_argument('--output', type=str, default=None,
                        help='Output ONNX file path')
    
    args = parser.parse_args()
    
    if args.input and args.output:
        export_to_onnx(args.input, args.output, args.model)
    else:
        export_full_model(args.model)
