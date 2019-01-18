
import numpy as np
import matplotlib.pyplot as plt
import os

server_types = ['blocking 1', 'blocking 2', 'nonblocking', 'asynchronous']
var_types = ['array size', 'number clients', 'time delta, ms']
metrics = ['on client time', 'process time', 'sort time']

if not os.path.exists('plots'):
	os.mkdir('plots')

for var_idx in range(3):
	for metric_idx in range(3):
		plt.figure()
		plt.grid()
		for server_idx in range(4):
			try:
				data = np.loadtxt('{}{}{}.txt'.format(server_idx, var_idx, metric_idx));
				plt.scatter(data[:,0], data[:,1], label=server_types[server_idx])
			except FileNotFoundError:
				pass
		plt.legend()
		plt.xlabel(var_types[var_idx])
		plt.ylabel(metrics[metric_idx] + ', ms')
		plt.savefig('plots/{}{}.pdf'.format(var_idx, metric_idx))
