# neural_net.py

import pandas as pd
import tensorflow as tf
from tensorflow import keras
import numpy as np
import matplotlib.pyplot as plt
from sklearn.model_selection import *
import sklearn.metrics as metr
import subprocess as s
# remove warnings
import warnings as w
w.filterwarnings("ignore", message="numpy.dtype size changed")
w.filterwarnings("ignore", message="numpy.ufunc size changed")


class NeuralNet:
    def __init__(self, name):
        self.EPOCHS = 15
        self.INPUT_VEC = 20
        self.name = name

        self.path = '../data/nn/{}/'.format(name)
        s.call("rm -r "+self.path, shell=True)
        s.call("mkdir "+self.path, shell=True)

        self.train_data, self.test_data, self.train_labels, self.test_labels = None, None, None, None
        self.model, self.history = None, None
        self.count = 1

    def set_train_data(self, big_data):
        print('Data set shape', big_data.shape)

        len = big_data.shape[1] - 1
        # slice data, where last column in 2D big_data is the expected output, other columns before are inputs
        # each row in big_data is a vector
        # random-state sets shuffling of test and train data constant for repeatable results
        self.train_data, self.test_data, self.train_labels, self.test_labels = train_test_split(big_data[:, :len], big_data[:, len], test_size=0.25)
        # print(self.train_data, '\n', self.test_data, '\n', self.train_labels, '\n', self.test_labels)

        print("Training set: {}".format(self.train_data.shape))
        print("Testing set:  {}".format(self.test_data.shape))

    def set_vector_data(self, in_vec, out):
        # format input vector to have constant number of vectors
        in_vec = np.asarray([self.set_vector(row) for row in in_vec])
        self.train_data, self.test_data, self.train_labels, self.test_labels = train_test_split(in_vec, out, test_size=0.25)

        print("Training set: {}".format(self.train_data.shape))
        print("Testing set:  {}".format(self.test_data.shape))

    def build_model(self):
        self.model = keras.Sequential([
            keras.layers.Dense(20, activation=tf.nn.relu, input_shape=(self.train_data.shape[1],), name='layer1'),
            keras.layers.Dense(20, activation=tf.nn.relu, name='layer2'),
            keras.layers.Dense(1, name='out')
        ])

        # optimizer = tf.keras.optimizers.RMSprop()
        optimizer = tf.keras.optimizers.Adam()

        self.model.compile(loss='mse',
                      # optimizer='adam',
                      optimizer=optimizer,
                      metrics=['mae', 'accuracy'])

        self.model.summary()

    def train_model(self):
        tbCallBack = keras.callbacks.TensorBoard(log_dir=self.path, histogram_freq=0, write_graph=True, write_images=True)
        checkpointer = keras.callbacks.ModelCheckpoint(filepath=self.path+'best_weight.hdf5', verbose=0, save_best_only=True)  # save best model

        self.history = self.model.fit(self.train_data, self.train_labels, epochs=self.EPOCHS,
                            # verbose: 0 for no logging to stdout, 1 for progress bar logging, 2 for one log line per epoch
                            batch_size=32, validation_split=0.2, verbose=2,
                            callbacks=[self.PrintDot(), tbCallBack, checkpointer])

    def check1(self):
        self.plot_history()

        [loss, mae, accuracy] = self.model.evaluate(self.test_data, self.test_labels, verbose=0)
        print("\nTesting set Mean Abs Error: " + str(round(mae, 2)))
        print('Average loss:', loss)
        print('Average accuracy', accuracy)

        print('\nCheck to see if above data makes sense...')
        for i in range(5):  # get 10 samples
            print('Predicted:', round(self.model.predict(self.test_data).flatten()[i],1), end=" | ")
            print('Actual:', self.test_labels[i])

    def check2(self):
        self.model.load_weights(self.path+'best_weight.hdf5')  # load weights from best model

        # Predict and measure RMSE
        pred = self.model.predict(self.test_data)
        score = np.sqrt(metr.mean_squared_error(pred, self.test_labels))
        print("Score (RMSE): {}".format(score))

        # Plot the chart
        self.chart_regression(pred.flatten(), self.test_labels)  # sort is True by default

    def save_nn(self):
        print('\nsaving models...\n\n')
        self.model.save(self.path+'model.h5')
        self.model.save('results/model_{}.h5'.format(self.EPOCHS))

    class PrintDot(keras.callbacks.Callback):
        def on_epoch_end(self, epoch, logs):
            if epoch % 100 == 0:
                print('')
            print('.', end='')

    def chart_regression(self, pred, y, sort=True):
        plt.figure()
        t = pd.DataFrame({'pred': pred, 'y': y.flatten()})
        if sort:
            t.sort_values(by=['y'], inplace=True)
        x = np.arange(t.shape[0])
        plt.scatter(x, t['pred'].tolist(), label='prediction', color='orange')
        plt.scatter(x, t['y'].tolist(), label='expected', color='blue')
        plt.ylabel('output')
        plt.legend()
        self.save_image('regression')

    def plot_history(self):
        plt.figure()
        plt.xlabel('Epoch')
        plt.ylabel('Mean Abs Error [1000$]')
        plt.plot(self.history.epoch, np.array(self.history.history['mean_absolute_error']), label='Train Loss')
        plt.plot(self.history.epoch, np.array(self.history.history['val_mean_absolute_error']), label='Val loss')
        plt.legend()
        self.save_image('history')

    def save_image(self, name):
        # fig = plt.gcf()
        # dpi = fig.get_dpi()
        # fig.set_size_inches(config.x_width / float(dpi), config.y_width / float(dpi))
        plt.savefig(self.path + name)
        plt.close()

    def set_vector(self, row): # zero padding for varying input sizes
        num_0 = self.INPUT_VEC - len(row)
        for i in range(num_0):
            row.append(0)
