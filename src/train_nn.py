# train_nn.py
import neural_net as nn
import numpy as np
import os


def main():
    # import data
    # create neural nets
    # sort data into appropriate neural net
    big_data = np.loadtxt(open("../data/nn_data.csv", "rb"), delimiter=",", skiprows=1)

    for i in range(8):
        print('\n\n\n--------------------------NEURAL NET V0_{}----------------------------\n\n'.format(i))
        test = nn.NeuralNet("V0_{}".format(i))
        data = big_data[np.where(big_data[:, 0] == i)]

        test.set_train_data(data)
        test.build_model()
        test.train_model()
        test.check1()
        test.check2()


if __name__ == '__main__':
    main()
