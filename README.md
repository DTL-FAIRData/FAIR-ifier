# The FAIRifier

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7ac974a8e9a44c7a8fcf5f1b14419d4a)](https://www.codacy.com/app/Shamanou/FAIRifier?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=DTL-FAIRData/FAIRifier&amp;utm_campaign=Badge_Grade)

[![Build Status](https://travis-ci.org/DTL-FAIRData/FAIRifier.svg?branch=development)](https://travis-ci.org/DTL-FAIRData/FAIRifier)

The FAIRifier is a tool to make messy data FAIR.
FAIR stands for Findable, Accessable, Interoperable, Reusable.
To summarize:
* Findable means the data has clearly described metadata, which makes it possible to find the data with a search engine.
* Accessable means that the accessability of the data is described in the metadata.
* Interoperable means it should be able to connect to other data, for example linked data technology.
* Reusable means that metadata contains specification about who and what can reuse it. 
* For more information about FAIR [click here](https://www.dtls.nl/fair-data/).

The infrastructure consists of three core elements. The FAIR search engine, which indexes all the data in the network. 
The FAIR Data Point(FDP), which contains the data every institution should host one or more FDP's. The FAIRifier is meant to convert any data
for a interoperable resource this can be put on a FDP within the FAIRifier.

The FAIRifier is based on Openrefine with the rdf-plugin added. Theres a extra option which enables the user
to PUSH their FAIR data to an [FDP(FAIR Data Point)](https://github.com/DTL-FAIRData/FAIRDataPoint).  

The FAIRifier is developed at [The Dutch tech centre for life sciences(DTL)](https://www.dtls.nl/)(the Dutch Tech Centre for life sciences).  
DTL’s mission is to establish an interconnected research infrastructure that enables cross-disciplinary life science research in national 
and international collaboration in a cost-effective manner.

## Getting Started

Dependencies:
  - Java 8
  - Apache Ant

### Prequisites
install depedencies (assuming Java 8 is installed)

```
sudo apt-get install ant
```

download the git repository
```
git clone --recursive -b development https://github.com/DTL-FAIRData/FAIRifier.git
```
cd to the directory of the git repo

### Building
```
cd FAIRifier/
```
and now build
```
./refine build
```

## Deployment

Run the ./refine file
```
./refine
```

You can find more information on the [FAIRifier wiki](https://github.com/DTL-FAIRData/FAIRifier/wiki).

## License
This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details 
