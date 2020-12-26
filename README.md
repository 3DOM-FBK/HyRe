Hybrid Registration  (HyRe)
===================  
  
Hybrid Registration is a command line software designed to analyze, co-register and filter airborne point clouds simultaneously acquired by LiDAR sensors and photogrammetric algorithm (dense image matching). 
The tool starts from semantically segmented dense point clouds and: 
1. uses sensor-specific and point-wise quality features of 3D points to locally keep only those points with the highest positional precision;
2. aggregates these quality features and evaluates them on a per-voxel and per-class basis
3. keeps the best points and uses them to refine the alignment of the LiDAR and photogrammetric point clouds, by minimizing the discrepancies iteratively within their overlap area.

Copyright (C) 2019  FBK-3DOM, 3dom@fbk.eu
  
This program comes with ABSOLUTELY NO WARRANTY.  
This is free software, and you are welcome to redistribute it under certain conditions;  
  
Usage: hyRe photo_file lidar_file voxelSide [-o (--output) output] [-v (--verbose) verbose] [-w (--overwrite) overWrite]
- voxelSide: the lenght of the voxel cube  
  
Example:
```
$ hyRe f1.txt f2.txt 1.0f -v
```
