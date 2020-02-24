## READ DATA

```java
prepare the output files

read and print the config.json

f1==rnd1.txt && f2==rnd2.txt
	generate random clouds

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
			evaluate prop_std

	for each point
		for each property prop
			normalize [0:1] prop

		load formula from config
		evaluate and set score

print runtime statistics

```


## GENERATE VOXEL STRUCTURE
```java
voxelSide != 0
	new VoxelGrid(points, bbox, voxelSide)

	generateVoxels()
		for each point
			evaluate voxel id
			add point to correct class voxelsList

```


## SHOW DATA
```java
voxelSide != 0
	random voxel v
	for each fileType
		getPoints(v) and print points

	evaluate average voxel density


print properties statistics

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
