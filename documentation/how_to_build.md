# How to build
To compile the Java code and create the necessary JARs to execute the Systematic-Trading program, there's software required and a straightforward build process to follow.

### First build
1. [Prerequisite software](#prerequisite-software)
2. [Initial checkout](#initial-checkout)
3. [Build](#build)

### After the first build
1. [Update](#update-with-no-local-changes)
2. [Build](#build)


## Prerequisite Software
Install these software versions or later, ensuring they're available on the system path.
- Java SE Development Kit 1.8.0_65
- Maven 3.2.5
- Git 2.11.0


## Initial checkout
- Open a terminal window
- Create your local working directory for the Systematic-Trading project
- Navigate to your Systematic-Trading working directory
- run `git clone https://github.com/CjHare/systematic-trading.git`


## Update (with no local changes)
- Navigate to your Systematic-Trading working directory
- run `git pull`

## Build
- Navigate to your Systematic-Trading working directory
- run `mvn clean install`
