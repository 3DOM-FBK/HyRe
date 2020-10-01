Hybrid Registration  
===============  
  
Hybrid Registration (C) 2019 is a command line software designed to analyze, co-register and filter airborne point clouds acquired by LiDAR sensors  and photogrammetric algorithm.  Copyright (C) 2019  Michele Welponer, mwelponer@gmail.com (Fondazione Bruno Kessler)
  
This program comes with ABSOLUTELY NO WARRANTY;  
This is free software, and you are welcome to redistribute it  
under certain conditions;  
  
Usage: pcFilter pc_file lidar_file voxelSide [-o (--output) output] [-v (--verbose) verbose] [-w (--overwrite) overWrite]
&nbsp;&nbsp;voxelSide: the lenght of the voxel cube  
  
Example:
&nbsp;&nbsp;$ pcFilter f1.txt f2.txt 1.0f -v


## READ DATA

```java
Main()
prepare the output files

read and print the config.json

f1==rnd1.txt && f2==rnd2.txt
	generate random clouds

PcFilter()
voxelSide != 0

	findMin()
		bbox(f1, f2)
		min bbox(f1, f2) // used to convert coordinates

for each filetype
	parseData()
		headers -> properties

		for each line
			extract x, y, z, subtract min bbox(f1, f2) // convert coordinates
			extract properties

			for each property prop
				adjust ScanAngleRank -> Math.abs
				evaluate prop_N, prop_sum, prop_mean

			add points a new point

	updateStatistics()
		for each point
			for each property prop
                std = pow((prop.val - prop_mean), 2)
                prop_std += std

        prop_std = sqrt(prop_std / prop_N)

		for each point
			for each property prop
				normalize [0:1] prop

			load formula from config
			evaluate and set score

print runtime statistics

```

## GENERATE VOXEL STRUCTURE
```java
VoxelGrid()
voxelSide != 0
	new VoxelGrid(points, bbox, voxelSide)

	generateVoxels()
		for each point
			evaluate voxel id
			add point to correct class voxelsList

```


## SHOW DATA
### PRINT PROPERTIES STATISTICS
```java
print properties statistics (on all data points)

	(for each fileType)
		for each property
			med, mad

	(for each fileType)
		for each property
			for each class
				mad, med

Main()
voxelSide != 0
```


### Check POINT BELONGING TO SPECIFIC VOXEL
	//random voxel v
	//for each fileType
	//	getPoints(v) and print points

### AVERAGE VOXEL DENSITY
```java
    for each fileType
        //evaluate average per class voxel density
        for each class
            getVoxels(fileType, class)
            for each voxel
                npointsInVoxel += pointsInVoxel.size
            mean = pointsInVoxel / voxels

            for each voxel
                std += pow((pointsInVoxel.size - mean, 2)
            std = sqrt(std / voxels)

        //evaluate average voxel density
        getVoxels(fileType)
        for each voxel
            npointsInVoxel += pointsInVoxel.size
        mean = pointsInVoxel / voxels

        for each voxel
            std += pow((pointsInVoxel.size - mean, 2)
        std = sqrt(std / voxels)
```      

### PHOTO/LIDAR INTERSECTION IN EACH VOXEL
```java
printMultiFileTypeVoxels()
    // find the set of voxels that contain both lidar and photo points
    for each fileType
        find intersection getVoxels(fileType)
    print count voxels in intersection set
```

### MULTICLASS IN EACH INTERSECTION VOXEL
```java
printMultiClassVoxels()
    // find the set of voxels that contain a combination of classes in lidar and
		// photo clouds
    classes = Combinator.generate()
    for each fileType
        for each combination
            print size of getVoxels(fileType, combination)
```

### FILTERED INTERSECTION SET
```java
printFilteredVoxels()
    // find (filteredIntersectionSet) the set of voxels that contain both lidar
    // and photo points (intersection) and at least one class where both fileTypes
    // meet density criteria (voxel_v density >= voxel density mean)
    // TODO: describe in pseudocode here (DONE.)
    for each v in intersection
        for each class
            for each fileType
                if (ftClVDensityMean == 0 || ftClVDensity < ftClVDensityMean) -> passed false, break
                else passed true

            if passed add v, break
```

### SCORED FILTERED INTERSECTION SET
```java
printScoredFilteredVoxels()
    // find (scoredFilteredIntersectionSet) the set of voxels that contain both lidar
    // and photo points (intersection) and at least one class where both fileTypes
    // meet density criteria (voxel_v density >= voxel density mean)
    // TODO: describe in pseudocode here (DONE.)
    for each v in filteredIntersectionSet
        for each class
            for each fileType
                getPoints(ft, v, pclass)
                if (points.size < ftClVDensityMean) -> passed false, break
                else passed true

            if passed add v, break
```

## WRITE DATA
```java
    for each fileType
        for v in scoredFilteredIntersectionSet
            for each point where score is ok
                write point to ft output
```