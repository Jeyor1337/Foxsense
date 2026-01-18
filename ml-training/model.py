import torch
import torch.nn as nn
import config


class AimNet(nn.Module):
    def __init__(self, input_dim=None, hidden_dims=None, output_dim=None, dropout_rate=None):
        super(AimNet, self).__init__()
        
        if input_dim is None:
            input_dim = config.INPUT_DIM
        if hidden_dims is None:
            hidden_dims = config.HIDDEN_DIMS
        if output_dim is None:
            output_dim = config.OUTPUT_DIM
        if dropout_rate is None:
            dropout_rate = config.DROPOUT_RATE
        
        layers = []
        prev_dim = input_dim
        
        for hidden_dim in hidden_dims:
            layers.append(nn.Linear(prev_dim, hidden_dim))
            layers.append(nn.ReLU())
            layers.append(nn.Dropout(dropout_rate))
            prev_dim = hidden_dim
        
        layers.append(nn.Linear(prev_dim, output_dim))
        
        self.network = nn.Sequential(*layers)
        
        self._init_weights()
    
    def _init_weights(self):
        for m in self.modules():
            if isinstance(m, nn.Linear):
                nn.init.kaiming_normal_(m.weight, mode='fan_in', nonlinearity='relu')
                if m.bias is not None:
                    nn.init.zeros_(m.bias)
    
    def forward(self, x):
        return self.network(x)


class AimNetWithResidual(nn.Module):
    def __init__(self, input_dim=None, hidden_dims=None, output_dim=None, dropout_rate=None):
        super(AimNetWithResidual, self).__init__()
        
        if input_dim is None:
            input_dim = config.INPUT_DIM
        if hidden_dims is None:
            hidden_dims = config.HIDDEN_DIMS
        if output_dim is None:
            output_dim = config.OUTPUT_DIM
        if dropout_rate is None:
            dropout_rate = config.DROPOUT_RATE
        
        self.input_proj = nn.Linear(input_dim, hidden_dims[0])
        
        self.blocks = nn.ModuleList()
        for i in range(len(hidden_dims) - 1):
            block = nn.Sequential(
                nn.Linear(hidden_dims[i], hidden_dims[i + 1]),
                nn.ReLU(),
                nn.Dropout(dropout_rate)
            )
            self.blocks.append(block)
            
            if hidden_dims[i] != hidden_dims[i + 1]:
                self.blocks.append(nn.Linear(hidden_dims[i], hidden_dims[i + 1]))
        
        self.output_layer = nn.Linear(hidden_dims[-1], output_dim)
        
        self._init_weights()
    
    def _init_weights(self):
        for m in self.modules():
            if isinstance(m, nn.Linear):
                nn.init.kaiming_normal_(m.weight, mode='fan_in', nonlinearity='relu')
                if m.bias is not None:
                    nn.init.zeros_(m.bias)
    
    def forward(self, x):
        x = self.input_proj(x)
        x = torch.relu(x)
        
        for block in self.blocks:
            x = block(x)
        
        return self.output_layer(x)


class AimNetLSTM(nn.Module):
    def __init__(self, input_dim=None, hidden_dim=64, num_layers=2, output_dim=None, dropout_rate=None):
        super(AimNetLSTM, self).__init__()
        
        if input_dim is None:
            input_dim = config.INPUT_DIM
        if output_dim is None:
            output_dim = config.OUTPUT_DIM
        if dropout_rate is None:
            dropout_rate = config.DROPOUT_RATE
        
        self.hidden_dim = hidden_dim
        self.num_layers = num_layers
        
        self.lstm = nn.LSTM(
            input_size=input_dim,
            hidden_size=hidden_dim,
            num_layers=num_layers,
            batch_first=True,
            dropout=dropout_rate if num_layers > 1 else 0
        )
        
        self.fc = nn.Linear(hidden_dim, output_dim)
    
    def forward(self, x):
        if x.dim() == 2:
            x = x.unsqueeze(1)
        
        lstm_out, _ = self.lstm(x)
        out = self.fc(lstm_out[:, -1, :])
        return out


def create_model(model_type='simple'):
    if model_type == 'simple':
        return AimNet()
    elif model_type == 'residual':
        return AimNetWithResidual()
    elif model_type == 'lstm':
        return AimNetLSTM()
    else:
        raise ValueError(f"Unknown model type: {model_type}")


def count_parameters(model):
    return sum(p.numel() for p in model.parameters() if p.requires_grad)


if __name__ == "__main__":
    model = create_model('simple')
    print(f"Simple model parameters: {count_parameters(model)}")
    
    dummy_input = torch.randn(1, config.INPUT_DIM)
    output = model(dummy_input)
    print(f"Input shape: {dummy_input.shape}")
    print(f"Output shape: {output.shape}")
