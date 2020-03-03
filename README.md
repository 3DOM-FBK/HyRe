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
```java
Main()
voxelSide != 0

### Check POINT BELONGING TO SPECIFIC VOXEL
	//random voxel v
	//for each fileType
	//	getPoints(v) and print points

### AVERAGE VOXEL DENSITY
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

### PHOTO/LIDAR INTERSECTION IN EACH VOXEL
    // find the set of voxels that contain both lidar and photo points
    for each fileType
        find intersection getVoxels(fileType)
    print count voxels in intersection set

### MULTICLASS IN EACH INTERSECTION VOXEL
    // find the set of voxels that contain both C0 & C1 lidar points
    //TODO: optimize combination without permutations
    for each fileType
        set c0_c1
        for v in intersection
            c0 = false
            c1 = false
            for each point
                if p.c0 -> c0 = true
                if p.c1 -> c1 = true
                if c0 && c1 -> add v in c0_c1
                break

    // find (filteredIntersectionSet) the set of voxels that contain both lidar
    // and photo points (intersection) and at least one class where both fileTypes
    // meet density criteria (voxel_v density >= voxel density mean)

print properties statistics (on all data points)

	(for each fileType)
		for each property
			med, mad

	(for each fileType)
		for each property
			for each class
				mad, med
```









## APPLY FILTER AND WRITE DATA
```java
for each filetype
	for each point
		load threshold from config
			score <= threshold
				write point to output
```
