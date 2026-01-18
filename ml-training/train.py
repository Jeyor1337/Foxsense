import os
import json
import torch
import torch.nn as nn
import torch.optim as optim
from tqdm import tqdm
import numpy as np
import pickle

import config
from dataset import prepare_data, load_all_data, filter_outliers, split_data, create_dataloaders
from model import create_model, count_parameters


class EarlyStopping:
    def __init__(self, patience=10, min_delta=0):
        self.patience = patience
        self.min_delta = min_delta
        self.counter = 0
        self.best_loss = None
        self.early_stop = False
    
    def __call__(self, val_loss):
        if self.best_loss is None:
            self.best_loss = val_loss
        elif val_loss > self.best_loss - self.min_delta:
            self.counter += 1
            if self.counter >= self.patience:
                self.early_stop = True
        else:
            self.best_loss = val_loss
            self.counter = 0


def train_epoch(model, train_loader, criterion, optimizer, device):
    model.train()
    total_loss = 0
    num_batches = 0
    
    for inputs, targets in train_loader:
        inputs = inputs.to(device)
        targets = targets.to(device)
        
        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, targets)
        loss.backward()
        
        torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
        
        optimizer.step()
        
        total_loss += loss.item()
        num_batches += 1
    
    return total_loss / num_batches


def validate(model, val_loader, criterion, device):
    model.eval()
    total_loss = 0
    num_batches = 0
    
    with torch.no_grad():
        for inputs, targets in val_loader:
            inputs = inputs.to(device)
            targets = targets.to(device)
            
            outputs = model(inputs)
            loss = criterion(outputs, targets)
            
            total_loss += loss.item()
            num_batches += 1
    
    return total_loss / num_batches


def evaluate(model, test_loader, device):
    model.eval()
    all_preds = []
    all_targets = []
    
    with torch.no_grad():
        for inputs, targets in test_loader:
            inputs = inputs.to(device)
            outputs = model(inputs)
            
            all_preds.append(outputs.cpu().numpy())
            all_targets.append(targets.numpy())
    
    preds = np.concatenate(all_preds, axis=0)
    targets = np.concatenate(all_targets, axis=0)
    
    mse = np.mean((preds - targets) ** 2)
    mae = np.mean(np.abs(preds - targets))
    
    yaw_mae = np.mean(np.abs(preds[:, 0] - targets[:, 0]))
    pitch_mae = np.mean(np.abs(preds[:, 1] - targets[:, 1]))
    
    return {
        'mse': float(mse),
        'mae': float(mae),
        'yaw_mae': float(yaw_mae),
        'pitch_mae': float(pitch_mae)
    }


def save_checkpoint(model, optimizer, epoch, loss, scaler, path):
    checkpoint = {
        'epoch': epoch,
        'model_state_dict': model.state_dict(),
        'optimizer_state_dict': optimizer.state_dict(),
        'loss': loss,
    }
    torch.save(checkpoint, path)
    
    if scaler is not None:
        scaler_path = path.replace('.pt', '_scaler.pkl')
        with open(scaler_path, 'wb') as f:
            pickle.dump(scaler, f)


def load_checkpoint(model, optimizer, path):
    checkpoint = torch.load(path)
    model.load_state_dict(checkpoint['model_state_dict'])
    optimizer.load_state_dict(checkpoint['optimizer_state_dict'])
    return checkpoint['epoch'], checkpoint['loss']


def train(model_type='simple', data_dir=None):
    os.makedirs(config.MODEL_DIR, exist_ok=True)
    
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Using device: {device}")
    
    print("Loading data...")
    train_loader, val_loader, test_loader, scaler = prepare_data(data_dir)
    
    print("Creating model...")
    model = create_model(model_type)
    model = model.to(device)
    print(f"Model parameters: {count_parameters(model)}")
    
    criterion = nn.MSELoss()
    optimizer = optim.Adam(
        model.parameters(),
        lr=config.LEARNING_RATE,
        weight_decay=config.WEIGHT_DECAY
    )
    
    scheduler = optim.lr_scheduler.ReduceLROnPlateau(
        optimizer,
        mode='min',
        factor=config.LR_SCHEDULER_FACTOR,
        patience=config.LR_SCHEDULER_PATIENCE
    )
    
    early_stopping = EarlyStopping(patience=config.EARLY_STOPPING_PATIENCE)
    
    best_val_loss = float('inf')
    best_model_path = os.path.join(config.MODEL_DIR, f'best_model_{model_type}.pt')
    
    history = {
        'train_loss': [],
        'val_loss': [],
        'lr': []
    }
    
    print("Starting training...")
    for epoch in range(config.EPOCHS):
        train_loss = train_epoch(model, train_loader, criterion, optimizer, device)
        val_loss = validate(model, val_loader, criterion, device)
        
        current_lr = optimizer.param_groups[0]['lr']
        history['train_loss'].append(train_loss)
        history['val_loss'].append(val_loss)
        history['lr'].append(current_lr)
        
        scheduler.step(val_loss)
        
        if val_loss < best_val_loss:
            best_val_loss = val_loss
            save_checkpoint(model, optimizer, epoch, val_loss, scaler, best_model_path)
        
        early_stopping(val_loss)
        
        if (epoch + 1) % 10 == 0 or epoch == 0:
            print(f"Epoch {epoch+1}/{config.EPOCHS} - "
                  f"Train Loss: {train_loss:.6f}, Val Loss: {val_loss:.6f}, "
                  f"LR: {current_lr:.6f}")
        
        if early_stopping.early_stop:
            print(f"Early stopping at epoch {epoch+1}")
            break
    
    print("\nLoading best model for evaluation...")
    checkpoint = torch.load(best_model_path)
    model.load_state_dict(checkpoint['model_state_dict'])
    
    print("Evaluating on test set...")
    metrics = evaluate(model, test_loader, device)
    print(f"Test MSE: {metrics['mse']:.6f}")
    print(f"Test MAE: {metrics['mae']:.6f}")
    print(f"Yaw MAE: {metrics['yaw_mae']:.4f} degrees")
    print(f"Pitch MAE: {metrics['pitch_mae']:.4f} degrees")
    
    history_path = os.path.join(config.MODEL_DIR, f'history_{model_type}.json')
    with open(history_path, 'w') as f:
        json.dump(history, f)
    
    metrics_path = os.path.join(config.MODEL_DIR, f'metrics_{model_type}.json')
    with open(metrics_path, 'w') as f:
        json.dump(metrics, f)
    
    print(f"\nTraining complete! Best model saved to: {best_model_path}")
    return model, scaler


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='Train AimNet model')
    parser.add_argument('--model', type=str, default='simple', 
                        choices=['simple', 'residual', 'lstm'],
                        help='Model type to train')
    parser.add_argument('--data', type=str, default=None,
                        help='Path to data directory')
    
    args = parser.parse_args()
    
    train(model_type=args.model, data_dir=args.data)
