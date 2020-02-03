## READ DATA

```java
voxelSide > 0
	min bboox(f1, f2)

for each filetype
	parseData()
	headers
	
	for each line
		extract x, y, z, subtract min bboox(f1, f2)
		extract properties
	
		for each property
			adjust ScanAngleRank
			evaluate sum, mean
	
	updateStatistics()
	for each point
		for each property
			evaluate std

	for each point
		for each property p
			normalize property p
		
		evaluate score 
			2 case

return -> linked list of points (mix of f1, f2)

print runtime statistics
```


## GENERATE STRUCTURE
```java
voxelSide > 0
	new VoxelGrid(points)
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