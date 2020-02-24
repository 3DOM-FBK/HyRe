## READ DATA

```java
prepare the output files

read and print the config.json

rnd1.txt & rnd2.txt
	generate random clouds

voxelSide > 0
	findMin()
		bboox(f1, f2)
		min bboox(f1, f2) // used to convert coordinates

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
voxelSide > 0
	random voxel v
		for each fileType
			getPoints(v)

	evaluate average voxel density


print properties statistics

(for each fileType)
	for each property
		med, mad

(for each fileType)
	for each property
		for each class
			mad, med


for each filetype
	for each point
		score

for each filetype
	show 1 point score
```


## APPLY FILTER AND WRITE DATA
```java
hashmap runtime statistics
parsing threshold.dat

for each filetype
	for each point
		class 0
			if score > threshold skip
		class 1
			if score > threshold skip
		class 2
			if score > threshold skip

		add min to x, y, z
		write out_f1.txt
```
