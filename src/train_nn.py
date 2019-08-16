# train_nn.py
import warnings as w
w.filterwarnings("ignore")
import neural_net as nn
import numpy as np
import brain


# read in global_config parameters shared between JAVA and Python
with open('global_config.txt', 'r') as f:
    input_list = [line.split() for line in f]

TP_ALIVE = int(input_list[0][1])  # num tp a scientist is alive in the model
MAX_IDEAS = int(input_list[1][1])  # same as in Config file, max number of ideas in action space
BETA = float(input_list[2][1])  # coefficient for NPV, determined by discount rate r ~ 3%


def main():
    # train_v0()
    train_v1()


def train_v0():
    print('\n<<<<<<<TRAIN_V0>>>>>>>>\n')
    # import data
    # create neural nets
    # sort data into appropriate neural net
    big_data = np.loadtxt(open("../data/nn/V0_data.csv", "rb"), delimiter=",", skiprows=1)  # age, q, T, max, mean, sds, impact_left
    net_list = []

    for i in range(TP_ALIVE):
        print('\n\n\n--------------------------NEURAL NET V0_{}----------------------------\n\n'.format(i))
        net = nn.NeuralNet("V0_{}".format(i))
        data = big_data[np.where(big_data[:, 0] == i)]

        net.set_train_data(data[:, 1:])  # slice out first column, which is the scientist's age
        net.build_model()
        net.train_model()
        net.save_model()

        net.check1()
        net.check2()

        net_list.append(net)

    print("\n\n\n\n\nDONE TRAINING V0's\n\n\n\n\n")


def train_v1():
    print('\n<<<<<<<TRAIN_V1>>>>>>>>\n')
    # load saved V0 neural net models
    print('loading Brains...')
    V0_net_list = []
    for i in range(TP_ALIVE):
        net = brain.Brain.load_brain("V0_{}".format(i))
        V0_net_list.append(net)

    big_data = np.loadtxt(open("../data/nn/V1_data.csv", "rb"), delimiter=",", skiprows=1)  # sci_id, tp, sci_age, actual_returns, ideas...
    idea_data = np.loadtxt(open("../data/model/perceived_ideas.csv", "rb"), delimiter=",", skiprows=1)  # sci, idea, max, mean, sds
    q_data = np.loadtxt(open("../data/model/num_k_total_idea_tp.csv", "rb"), delimiter=",", skiprows=1)[:, 1:]
    T_data = np.loadtxt(open("../data/model/T_total_idea_tp.csv", "rb"), delimiter=",", skiprows=1)[:, 1:]

    V1_net_list = [brain.Brain(None)] * TP_ALIVE  # initiate list of placeholder neural nets

    age = TP_ALIVE - 1
    while age >= 0:
        print('\n\n\n--------------------------NEURAL NET V1_{}----------------------------\n\n'.format(age))
        net = nn.NeuralNet("V1_{}".format(age))
        data = big_data[np.where(big_data[:, 2] == age)]
        V1_train_data = []

        for row in data:  # process each action space across all action spaces given specific scientist age
            V1_in_vec = []
            sci_id = int(row[0])
            tp = int(row[1])
            for idea_idx in row[4:]:  # get V0 for each idea in the action space
                idea_idx = int(idea_idx)
                if idea_idx == -1: break  # -1 signals end of idea action space vector, no more ideas in this action space

                q = q_data[idea_idx, tp]
                T = T_data[idea_idx, tp]

                query1 = idea_data[np.where(idea_data[:, 0] == sci_id)]
                idea_row = query1[np.where(query1[:, 1] == idea_idx)][0]  # fetch 1d list, which is the only element in a 2d list
                maxx = idea_row[2]
                mean = idea_row[3]
                sds = idea_row[4]

                input_data = [q, T, maxx, mean, sds]
                V0 = V0_net_list[age].predict(np.asarray(input_data))  # get V0 based on state space of each idea
                V1_in_vec.append(V0)

            while len(V1_in_vec) < MAX_IDEAS:  # convert all the -1's of nonexist ideas to potential impact of 0
                V1_in_vec.append(0)  # zero padding the input layer of the neural net

            # calculate the true valuation returns of the scientist's action space to train against as output
            U_e = row[3]  # actual returns U_e used to train V_a, different from Smart_Optimize.java
            if age == TP_ALIVE - 1:  # backwards recursion, last age before scientist "death" has no NPV, just based on present returns
                true_out = U_e  # V7 = U(e7) if TP_ALIVE == 7
            else:  # backwards recursion where scientist accounts for both returns in present + future returns, predict V_(alpha+1) based on action space for current age alpha
                true_out = U_e + BETA * V1_net_list[age + 1].predict(np.asarray(V1_in_vec))  # V6 = U(e6) + BETA * V7 --> alpha from 0 to TP_ALIVE - 2

            V1_in_vec.append(true_out)  # add output as the last element of the input layer, follows format of training in the neural net class
            V1_train_data.append(np.asarray(V1_in_vec))  # add this action space to the set of all action spaces used to train V1

        print(np.asarray(V1_train_data))
        net.set_train_data(np.asarray(V1_train_data))
        net.build_model()
        net.train_model()
        net.save_model()
        net.check1()
        net.check2()

        load_net = brain.Brain.load_brain("V1_{}".format(age))  # NOTE: adding Brain class as element, not the Neural Net class
        V1_net_list[age] = load_net  # add current net --> V_age to its respective index/position in V1_net_list by age
        age -= 1  # train next neural net according to backwards recursion

    print("\n\n\n\n\nDONE TRAINING V1's\n\n\n\n\n")


if __name__ == '__main__':
    main()
