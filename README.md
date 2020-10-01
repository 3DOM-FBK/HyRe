Hybrid Registration  (pcFilter)
===============  
  
Hybrid Registration (C) 2019 is a command line software designed to analyze, co-register and filter airborne point clouds acquired by LiDAR sensors  and photogrammetric algorithm. 
Copyright (C) 2019  Michele Welponer, mwelponer@gmail.com (Fondazione Bruno Kessler)
  
This program comes with ABSOLUTELY NO WARRANTY;  
This is free software, and you are welcome to redistribute it  
under certain conditions;  
  
Usage: hyRe photo_file lidar_file voxelSide [-o (--output) output] [-v (--verbose) verbose] [-w (--overwrite) overWrite]
- voxelSide: the lenght of the voxel cube  
  
Example:
```
$ hyRe f1.txt f2.txt 1.0f -v
```