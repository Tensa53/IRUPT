#!/bin/sh

cd ../data/
python dataprep.py
cd algorithms/add_greedy/
python add_greedy.py
cd ../divga
matlab matlab -nodisplay -nosplash -nodesktop -r "run('DIVGA.m');exit;"
cd ../igdec_qaoa/
python loch_qaoa_tcs.py
python noise_loch_qaoa_tcs.py
cd ../select_qaoa/
python select_qaoa.py ideal
python select_qaoa.py noise