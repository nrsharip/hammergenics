## Overview
**HammerGenics** is a simple, extensible (with [Groovy](https://en.wikipedia.org/wiki/Apache_Groovy) - **under development**) 3D sanbox game engine built on top of the [LibGDX](https://github.com/libgdx/libgdx) ecosystem and [Bullet3](https://github.com/bulletphysics/bullet3) physics engine. It has been designed with an idea that the game development could be as simple, seamless and joyful as playing with [Lego](https://en.wikipedia.org/wiki/Lego).

Major features introduced in current version are:
- **Material Attributes editor**: the UI and underlying structures to edit colors, textures and lights
- **Mesh editor**: including node transforms and render, vertex analysis for primitive shapes (used for tiles in map/terrain generation)
- **Animation editor**: allows to create the keyframes and edit the bones affected with the timeline frame slider 
- **Map Generator**: with the help of [noise4j](https://github.com/czyzby/noise4j) allows to generate the terrain chunk grids and apply the tiles, which are primitive terrain parts in the form of a squerish mesh
- **Physics**: currently all the models that are being added to the project have rigid body created and being added to the Dynamics World ([Bullet3](https://github.com/bulletphysics/bullet3)) engine
- **AI Algorithms**: currently implemented the following features from [gdx-ai](https://github.com/libgdx/gdx-ai) library: [Steering](https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors), [Formations](https://github.com/libgdx/gdx-ai/wiki/Formation-Motion) and [Pathfinding Algorithms](https://github.com/libgdx/gdx-ai/wiki/Pathfinding)

## Presentations

**CREDITS**: assets by [Kenney](https://kenney.nl/) ([Animated Characters 1](https://kenney.nl/assets/animated-characters), [Car Kit](https://kenney.nl/assets/car-kit), [Weapon Pack](https://kenney.nl/assets/weapon-pack) etc.)

<table>
  <tr>
    <td width="50%"><img src="/docs/gifs/attr_1.gif?raw=true" width="100%"></td>
    <td width="50%"><img src="/docs/gifs/mesh_2.gif?raw=true" width="100%"></td>
  </tr>
  <tr>
    <td align="center">Material Attributes</td>
    <td align="center">Mesh Editor</td>
  </tr>
  <tr>
    <td><img src="/docs/gifs/anim_character_2.gif?raw=true" width="100%"></td>
    <td><img src="/docs/gifs/map_aggregated_1.gif?raw=true" width="100%"></td>
  </tr>
  <tr>
    <td align="center">Animations</td>
    <td align="center">Map Generation</td>
  </tr>
  <tr>
    <td><img src="/docs/gifs/physics_weapons_kit_4.gif?raw=true" width="100%"></td>
    <td><img src="/docs/gifs/ai_aggregated_1.gif?raw=true" width="100%"></td>
  </tr>
  <tr>
    <td align="center">Physics</td>
    <td align="center">AI Algorithms</td>
  </tr>
</table>

## License
**HammerGenics** is released under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). 

As an example of another well-known projects under the very same license think of [Android OS](https://source.android.com/), [Spring Framework](https://github.com/spring-projects/spring-framework), [Gradle](https://github.com/gradle/gradle) or [Apache HTTP Server](https://github.com/apache/httpd) (or any of these [projects](https://en.wikipedia.org/wiki/Category:Software_using_the_Apache_license)) which are installed and used by the majority of people and organizations accross the globe. 

Address any licensing concerns or considerations regarding the use of HammerGenics as you were dealing with any of these projects.

## Development
1. [Prerequisites](#prerequisites)
   1. [Download Java Development Kit 8](#download-java-development-kit-8)
   1. [`git clone` or download the sources](#git-clone-or-download-the-sources)
1. [Project Structure](#project-structure)
   1. [`:core` Project structure](#core-project-structure)
   1. [Credits for libraries](#credits-for-libraries)
1. [Running HammerGenics](#running-hammergenics)
   1. [Running `:desktop` Project](#running-desktop-project)

## Prerequisites

### Download Java Development Kit 8

Choose one of the available distributors and download JDK 8:
1. [Adoptium.net](https://adoptium.net/) (former AdoptOpenJdk)
1. [Amazon Corretto](https://aws.amazon.com/ru/corretto/)

### `git clone` or download the sources

Do one of the following:

`git clone https://github.com/nrsharip/hammergenics.git`

**OR**

Download the [ZIP archive](https://github.com/nrsharip/hammergenics/archive/refs/heads/main.zip) with the source code of this repo.

## Project Structure

Run the following command to see the list of the projects in the build: `.\gradlew.bat projects`.

The output should look like this:
```
------------------------------------------------------------
Root project
------------------------------------------------------------

Root project 'hammergenics'
+--- Project ':android'
+--- Project ':core'
+--- Project ':desktop'
+--- Project ':html'
\--- Project ':ios'
```

Other useful commands:
1. `.\gradlew.bat model` (also for project specific info, e.g. `.\gradlew.bat core:model`)
1. `.\gradlew.bat properties` (also for project specific info, e.g. `.\gradlew.bat core:properties`)
1. `.\gradlew.bat tasks -all` (also for project specific info, e.g. `.\gradlew.bat core:tasks`)

### `:core` Project structure

The following is a high-level component diagram to show the folder/package structure of the `:core` Project and the dependencies these packages have on the external libraries 

<p align="center"><img src="/docs/drawings/component_overall.png?raw=true" width="80%"></p>

### Credits for libraries

|Dependency|Authors|License|
|---|:---:|---|
|[com.badlogicgames.gdx:gdx](https://github.com/libgdx/libgdx):1.10.0|[authors file](https://github.com/libgdx/libgdx/blob/master/AUTHORS)|[license](https://github.com/libgdx/libgdx/blob/master/LICENSE)|
|[com.badlogicgames.gdx:gdx-ai](https://github.com/libgdx/gdx-ai):1.8.2|[davebaol](https://github.com/davebaol)|[license](https://github.com/libgdx/gdx-ai/blob/master/LICENSE)|
|[com.badlogicgames.gdx:gdx-bullet](https://github.com/libgdx/libgdx):1.10.0|[authors file](https://github.com/libgdx/libgdx/blob/master/AUTHORS)|[license](https://github.com/libgdx/libgdx/blob/master/LICENSE)|
|[com.github.czyzby:noise4j](https://github.com/czyzby/noise4j):0.1.0|[czyzby](https://github.com/czyzby)|[license](https://github.com/czyzby/noise4j/blob/master/LICENSE.md)|
|[com.kotcrab.vis:vis-ui](https://github.com/kotcrab/vis-ui):1.5.0|[Pawel Pastuszak](mailto:contact@kotcrab.com)|[license](https://github.com/kotcrab/vis-ui/blob/master/LICENSE)|
|[com.github.mgsx-dev.gdx-gltf:gltf](https://github.com/mgsx-dev/gdx-gltf):1.0.0|[mgsx-dev](https://github.com/mgsx-dev)|[license](https://github.com/mgsx-dev/gdx-gltf/blob/master/LICENSE)|
|[com.github.Anuken:GdxGifRecorder](https://github.com/Anuken/GDXGifRecorder):1.4|[Anuken](https://github.com/Anuken)|[license](https://github.com/Anuken/GDXGifRecorder)|
|[com.strongjoshua:libgdx-inGameConsole](https://github.com/StrongJoshua/libgdx-inGameConsole):1.0.0|[StrongJoshua](https://github.com/StrongJoshua)|[license](https://github.com/StrongJoshua/libgdx-inGameConsole/blob/master/LICENSE)|
|[com.github.MarcinSc:libgdx-graph](https://github.com/MarcinSc/libgdx-graph):9f6c886795ad1a201e3140fef262ee5a86f0cac1|[Marcin Sciesinski](https://github.com/MarcinSc)|[license](https://github.com/MarcinSc/libgdx-graph/blob/master/LICENSE)|
|[org.codehaus.groovy:groovy-all](https://mvnrepository.com/artifact/org.codehaus.groovy/groovy-all):3.0.8|see [wiki](https://en.wikipedia.org/wiki/Apache_Groovy)||

## Running HammerGenics

### Running `:desktop` Project

`.\gradlew.bat desktop:run`

see also [Command Line Interface](https://libgdx.com/dev/import-and-running/#command-line) from LibGDX.
