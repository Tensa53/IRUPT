#!/bin/sh

cd ../data/
python dataprep.py junit
python dataprep.py jmh
cd algorithms/add_greedy/
python add_greedy.py junit
python add_greedy.py jmh
cd ../divga
matlab matlab -nodisplay -nosplash -nodesktop -r "run('DIVGA_junit.m');exit;"
matlab matlab -nodisplay -nosplash -nodesktop -r "run('DIVGA_jmh.m');exit;"
cd ../igdec_qaoa/
python igdec_qaoa_tcs.py junit
python igdec_qaoa_tcs.py jmh
python noise_igdec_qaoa_tcs.py junit
python noise_igdec_qaoa_tcs.py jmh
cd ../qaoa_tcs/
python select_qaoa.py ideal junit
python select_qaoa.py ideal jmh
python select_qaoa.py noise junit
python select_qaoa.py noise jmh
