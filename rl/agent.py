import torch
import torch.nn as nn
import torch.optim as optim
import random
import numpy as np
from collections import deque
import os

# use GPU if available
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


# --- define neural network ---
class DQN(nn.Module):
    def __init__(self, state_size):
        """
        Single-head DQN network with unified action space (18 actions).
        """
        super(DQN, self).__init__()

        # shared layers
        self.net = nn.Sequential(
            nn.Linear(state_size, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU(),
            nn.Linear(128, 18)  # unified 18-action output
        )

    def forward(self, x):
        return self.net(x)  # [batch, 18] Q-values


# --- Agent Class ---
class Agent:
    def __init__(self, state_size, batch_size=32, gamma=0.99):
        print('reset Agent ...')
        self.state_size = state_size
        self.batch_size = batch_size
        self.gamma = gamma

        # target network update cycle
        self.target_update_frequency = 10000
        self.train_count = 0

        # unified action space (3 x 3 x 2 = 18)
        # moveX: -1,0,1 / moveY: -1,0,1 / shoot: False, True
        self.action_list = []
        for mx in [-1, 0, 1]:
            for my in [-1, 0, 1]:
                for s in [False, True]:
                    self.action_list.append((mx, my, s))

        # networks
        self.model = DQN(state_size).to(device)
        self.target_model = DQN(state_size).to(device)
        self.target_model.load_state_dict(self.model.state_dict())

        # optimizer & loss
        self.optimizer = optim.Adam(self.model.parameters(), lr=0.0001)
        self.criterion = nn.MSELoss()

        # replay buffer
        self.memory = deque(maxlen=500000)

        # epsilon-greedy settings
        self.epsilon = 1.0          # start exploration at 100%
        self.epsilon_min = 0.01     # minimum exploration rate
        self.epsilon_decay = 0.99990 # decay per training step
        self.train_start = 1000     # start training after N samples

        # gradient clipping for stability
        self.max_grad_norm = 10.0

        print("reset Agent complete")

    # --- Action encoding/decoding ---
    def _action_dict_to_index(self, action_dict):
        """Convert ActionPacket dict -> action index (0~17)."""
        mx = action_dict["moveX"]
        my = action_dict["moveY"]
        s = bool(action_dict["shoot"])
        for i, (ax, ay, shoot_flag) in enumerate(self.action_list):
            if ax == mx and ay == my and shoot_flag == s:
                return i
        # fallback (should not happen ideally)
        return 0

    def _index_to_action_dict(self, idx):
        """Convert action index (0~17) -> ActionPacket dict."""
        mx, my, s = self.action_list[idx]
        return {
            "moveX": mx,
            "moveY": my,
            "shoot": s
        }

    # --- choose action ---
    def get_action(self, state):
        # epsilon-greedy exploration
        if np.random.rand() <= self.epsilon:
            action_idx = random.randrange(len(self.action_list))
        else:
            state_tensor = self._state_to_tensor(state)
            with torch.no_grad():
                q_values = self.model(state_tensor)  # [1, 18]
            action_idx = torch.argmax(q_values, dim=1).item()

        # convert to ActionPacket format for Java
        return self._index_to_action_dict(action_idx)

    # --- store experience ---
    def append_sample(self, state, action_dict, reward, next_state, done):
        action_idx = self._action_dict_to_index(action_dict)
        self.memory.append((state, action_idx, reward, next_state, done))

    # --- training step ---
    def train_model(self):
        """Samples from replay buffer and trains the network."""
        if len(self.memory) < self.train_start:
            return

        batch = random.sample(self.memory, self.batch_size)

        # convert batch to tensors on the correct device
        states      = torch.from_numpy(
            np.array([x[0] for x in batch], dtype=np.float32)
        ).to(device)
        actions     = torch.from_numpy(
            np.array([x[1] for x in batch], dtype=np.int64)
        ).to(device)
        rewards     = torch.from_numpy(
            np.array([x[2] for x in batch], dtype=np.float32)
        ).to(device)
        next_states = torch.from_numpy(
            np.array([x[3] for x in batch], dtype=np.float32)
        ).to(device)
        dones       = torch.from_numpy(
            np.array([float(x[4]) for x in batch], dtype=np.float32)
        ).to(device)

        # Current Q(s,a)
        curr_q_all = self.model(states)                       # [B, 18]
        curr_q = curr_q_all.gather(1, actions.unsqueeze(1))   # [B, 1]
        curr_q = curr_q.squeeze(1)                            # [B]

        # Target Q = r + γ max_a' Q_target(s')
        with torch.no_grad():
            next_q_all = self.target_model(next_states)       # [B, 18]
            max_next_q, _ = next_q_all.max(dim=1)             # [B]

        target_q = rewards + (1.0 - dones) * self.gamma * max_next_q

        # compute loss and optimize
        loss = self.criterion(curr_q, target_q)

        self.optimizer.zero_grad()
        loss.backward()
        nn.utils.clip_grad_norm_(self.model.parameters(), self.max_grad_norm)
        self.optimizer.step()

        # periodic target network update
        self.train_count += 1
        if self.train_count % self.target_update_frequency == 0:
            self.update_target_model()
            print(f"Target model updated (Train count: {self.train_count}, loss={loss.item():.4f})")

        # decay epsilon
        if self.epsilon > self.epsilon_min:
            self.epsilon *= self.epsilon_decay
            if self.epsilon < self.epsilon_min:
                self.epsilon = self.epsilon_min

    def _state_to_tensor(self, state):
        """Convert 1D state (list or np.array) to [1, state_size] tensor on device."""
        arr = np.array(state, dtype=np.float32)
        return torch.from_numpy(arr).unsqueeze(0).to(device)

    def remember(self, *args):
        self.append_sample(*args)

    def train_step(self):
        self.train_model()

    def update_target_model(self):
        self.target_model.load_state_dict(self.model.state_dict())

    def load_model(self, filepath='model.pth'):
        if os.path.exists(filepath):
            self.model.load_state_dict(torch.load(filepath, map_location=device))
            self.update_target_model()
            self.epsilon = self.epsilon_min  # use minimal exploration for loaded model
            print(f"Model loaded from {filepath}")
        else:
            print("Saved model not found")

    def save_model(self, filepath=None):
        base_dir = os.path.dirname(os.path.abspath(__file__))

        if filepath is None:
            filepath = os.path.join(base_dir, "save_model", "model.pth")

        save_dir = os.path.dirname(filepath)
        os.makedirs(save_dir, exist_ok=True)
        torch.save(self.model.state_dict(), filepath)
        print(f"Model saved to {filepath}")
