import numpy as np
import pandas as pd
from matplotlib import pyplot as plt

df = pd.read_csv(r"C:\Users\buinn\Repos\FederatedLearningTestbed\Scripts\testbed\summary-accuracy-5clients.csv")
df.index = np.arange(1, len(df) + 1)
df.plot(kind='line', marker='^').grid(axis='y')

plt.yticks(np.arange(0.1, 0.9, 0.05))

plt.title('Accuracy over rounds - 5 clients version')
plt.ylabel('Accuracy')
plt.xlabel('#rounds')

plt.show()