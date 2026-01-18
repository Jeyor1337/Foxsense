import os
import json
import glob
import numpy as np
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.preprocessing import StandardScaler
import config


class AimDataset(Dataset):
    def __init__(self, data, input_scaler=None, fit_scaler=False):
        self.input_features = config.INPUT_FEATURES
        self.output_features = config.OUTPUT_FEATURES
        
        inputs = []
        outputs = []
        
        for sample in data:
            input_vec = [sample.get(f, 0.0) for f in self.input_features]
            output_vec = [sample.get(f, 0.0) for f in self.output_features]
            inputs.append(input_vec)
            outputs.append(output_vec)
        
        self.inputs = np.array(inputs, dtype=np.float32)
        self.outputs = np.array(outputs, dtype=np.float32)
        
        if config.NORMALIZE_INPUT:
            if fit_scaler:
                self.input_scaler = StandardScaler()
                self.inputs = self.input_scaler.fit_transform(self.inputs)
            elif input_scaler is not None:
                self.input_scaler = input_scaler
                self.inputs = self.input_scaler.transform(self.inputs)
            else:
                self.input_scaler = None
        else:
            self.input_scaler = None
        
        self.inputs = torch.from_numpy(self.inputs)
        self.outputs = torch.from_numpy(self.outputs)
    
    def __len__(self):
        return len(self.inputs)
    
    def __getitem__(self, idx):
        return self.inputs[idx], self.outputs[idx]
    
    def get_scaler(self):
        return self.input_scaler


def load_all_data(data_dir=None):
    if data_dir is None:
        data_dir = config.DATA_DIR
    
    all_samples = []
    json_files = glob.glob(os.path.join(data_dir, "*.json"))
    
    print(f"Found {len(json_files)} data files")
    
    for file_path in json_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                if 'samples' in data:
                    all_samples.extend(data['samples'])
                elif isinstance(data, list):
                    all_samples.extend(data)
        except Exception as e:
            print(f"Error loading {file_path}: {e}")
    
    print(f"Loaded {len(all_samples)} total samples")
    return all_samples


def filter_outliers(samples, percentile=99):
    if not samples:
        return samples
    
    yaw_changes = [abs(s.get('outputDeltaYaw', 0)) for s in samples]
    pitch_changes = [abs(s.get('outputDeltaPitch', 0)) for s in samples]
    
    yaw_threshold = np.percentile(yaw_changes, percentile)
    pitch_threshold = np.percentile(pitch_changes, percentile)
    
    filtered = [
        s for s in samples
        if abs(s.get('outputDeltaYaw', 0)) <= yaw_threshold
        and abs(s.get('outputDeltaPitch', 0)) <= pitch_threshold
    ]
    
    print(f"Filtered {len(samples) - len(filtered)} outliers")
    return filtered


def split_data(samples, train_ratio=0.8, val_ratio=0.1):
    np.random.seed(config.RANDOM_SEED)
    np.random.shuffle(samples)
    
    n = len(samples)
    train_end = int(n * train_ratio)
    val_end = int(n * (train_ratio + val_ratio))
    
    train_data = samples[:train_end]
    val_data = samples[train_end:val_end]
    test_data = samples[val_end:]
    
    return train_data, val_data, test_data


def create_dataloaders(train_data, val_data, test_data, batch_size=None):
    if batch_size is None:
        batch_size = config.BATCH_SIZE
    
    train_dataset = AimDataset(train_data, fit_scaler=True)
    scaler = train_dataset.get_scaler()
    
    val_dataset = AimDataset(val_data, input_scaler=scaler)
    test_dataset = AimDataset(test_data, input_scaler=scaler)
    
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size, shuffle=False)
    test_loader = DataLoader(test_dataset, batch_size=batch_size, shuffle=False)
    
    return train_loader, val_loader, test_loader, scaler


def prepare_data(data_dir=None):
    samples = load_all_data(data_dir)
    
    if not samples:
        raise ValueError("No data found!")
    
    samples = filter_outliers(samples)
    train_data, val_data, test_data = split_data(samples)
    
    print(f"Train: {len(train_data)}, Val: {len(val_data)}, Test: {len(test_data)}")
    
    return create_dataloaders(train_data, val_data, test_data)


if __name__ == "__main__":
    train_loader, val_loader, test_loader, scaler = prepare_data()
    
    for inputs, outputs in train_loader:
        print(f"Input shape: {inputs.shape}")
        print(f"Output shape: {outputs.shape}")
        print(f"Sample input: {inputs[0]}")
        print(f"Sample output: {outputs[0]}")
        break
