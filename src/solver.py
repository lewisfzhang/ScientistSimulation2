# solver.py
# solves systems of equations using sympy

# from sympy import symbols, solve

import numpy as np
from scipy.optimize import fsolve

e, q, M, lam, sig = [], [], [], [], []  # lam = perceived mean of idea, not lagrange

with open('in.txt', 'r') as fp:
    for line in fp:
        val = line.split()
        e.append(float(val[0]))
        q.append(int(val[1]))
        M.append(float(val[2]))
        lam.append(float(val[3]))
        sig.append(float(val[4]))

print(e, q, M, lam, sig)
    # each line has M, e, q. lam, sig, --> we will create lambda
    # iterate through, each new line represents an additional idea


def func(X):
    x = X[0]
    y = X[1]
    L = X[2] # this is the multiplier. lambda is a reserved keyword in python
    return x + y + L * (x**2 + y**2 - 1)


def dfunc(X):
    dLambda = np.zeros(len(X))
    h = 1e-3 # this is the step size used in the finite difference.
    for i in range(len(X)):
        dX = np.zeros(len(X))
        dX[i] = h
        dLambda[i] = (func(X+dX)-func(X-dX))/(2*h)
    return dLambda


# this is the max
X1 = fsolve(dfunc, [1, 1, 0])
print(X1, func(X1))

# this is the min
X2 = fsolve(dfunc, [-1, -1, 0])
print(X2, func(X2))

with open('out.txt', 'w') as f:
    f.write(str(X1) + str(func(X1)) + "\n" + str(X2) + str(func(X2)) + "\n")
