# brain.py
# the decision making process based on trained neural net and optimized with approximate DP
# acts as an artificial server

import warnings as w
w.filterwarnings("ignore")
from tensorflow import keras
import numpy as np
import subprocess as s


class Brain:
    # load the neural network into the brain
    def __init__(self, model):
        self.model = model

    # data is one input vector (1d numpy array), returns a scalar value as output
    def predict(self, data, return_neg=False):
        out = self.model.predict(np.asarray([data, data])).flatten()[0]  # predict value, weird formatting due to parameter requirements of predict() method

        if out < 0 and not return_neg: return 0  # don't return negative values because idea potential should never be negative --> negative due to nature of regression NN with actual value near 0
        else: return out

    @staticmethod
    def load_brain(name):
        git_dir = s.Popen('git rev-parse --show-toplevel', shell=True, stdout=s.PIPE).communicate()[0].decode("utf-8")[:-1]
        path = '/Users/conradmilhaupt/Documents/ScientistSimulation2/data/nn/{0}/model.h5'.format(name)
        return Brain(keras.models.load_model(path))
