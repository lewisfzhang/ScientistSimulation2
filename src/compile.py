from distutils.core import setup
from distutils.extension import Extension
from Cython.Distutils import build_ext

def main():
    ext_modules = [Extension("solver",  ["solver.py"])]

    setup(
        name='solver_c',
        cmdclass={'build_ext': build_ext},
        ext_modules=ext_modules
    )

from logic import main      # this comes from a compiled binary
main()