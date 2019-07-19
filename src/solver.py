# solver.py
# solves systems of equations using sympy

# from sympy import symbols, solve
from scipy.optimize import fsolve
from math import exp

def equations(p):
    x, y = p
    return (x+y**2-4, exp(x) + x*y - 3)

x, y =  fsolve(equations, (1, 1))

print(equations((x, y)))
