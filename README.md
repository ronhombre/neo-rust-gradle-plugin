# Neo Rust Gradle Plugin
A Gradle Plugin to build Rust projects using Gradle as the Build Environment instead of natively using Cargo.
Gradle is a powerful build tool and utilizing it for Rust projects brings massive benefits as it reduces the complexity
especially when it comes to projects working with multiple programming languages. Such an example is an Android app that
compiles Rust code to JNI binaries.

This plugin aims to make it seamless to run Rust projects with Gradle while maintaining a familiar environment to Rust
developers.

(Note: This is not a fork of rust-gradle-plugin. This was made and designed from scratch.)

## Project Versioning
This project follows the [Semantic Versioning 2.0](https://semver.org/) when updating the version number. As it stands,
the project is still under development and the API might change between MINOR versions(that is the **y** in x.y.z).

## Features

| Plugin Task    | Gradle Task      | Cargo Equivalent | Status | Since | Remarks               |
|----------------|------------------|------------------|:------:|:-----:|:----------------------|
| `CargoBuild`   | `gradle build`   | `cargo build`    |  Full  | 0.1.0 | Based on Cargo Docs*  |
| N/A            | `gradle clean`   | `cargo clean`    |  Semi  | 0.1.0 | No options passable** |
| `CargoBench`   | `gradle bench`   | `cargo bench`    |  Full  | 0.1.0 | Based on Cargo Docs*  |
| `CargoTest`    | `gradle test`    | `cargo test`     |  Full  | 0.1.0 | Based on Cargo Docs*  |
| `CargoPublish` | `gradle publish` | `cargo publish`  |  Full  | 0.1.0 | Based on Cargo Docs*  |

_*_ Based on [Cargo Docs](https://doc.rust-lang.org/cargo/commands).

_**_ Executes a simple `cargo clean` command.

| Status  | Definition                                            |
|---------|-------------------------------------------------------|
| Full    | Fully supported all command options.                  |
| Semi    | Limited support that might not allow command options. |
| WIP     | It is being implemented at the moment.                |
| TBD     | Planned to be done in the future.                     |
| No Plan | Avoided implementing due to design or logic reasons.  |

### Status
It can do all of the above with a Cargo.toml file included in the directory.

## Sample Code
### Gradle Kotlin DSL (build.gradle.kts)
```kotlin
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoTest

plugins {
    id("asia.hombre.neorust") version "0.1.0"
}

//Rust Configuration (Global-level)
rust {
    target = "x86-unknown-linux-gnu"

    benchmarking {
        //Benchmarking Configuration (Top-level)
        packageSelect = "packageb"
    }

    building {
        //Building Configuration (Top-level)
        packageSelect = "packagea"
        release = true
        target = "x86_64-unknown-linux-gnu"
    }

    publishing {
        //Publishing Configuration (Top-level)
        token = "TOKEN"
    }

    testing {
        //Testing Configuration (Top-level)
        packageSelect = "packageb"
    }
}

tasks.getByName<CargoBench>("bench") {
    //Benchmarking Configuration (Task-level)
    target = "armeabi-unknown-linux-gnu"
    noRun = true
}

tasks.getByName<CargoBuild>("build") {
    //Building Configuration (Task-level)
    target = "armeabi-unknown-linux-gnu"
    workspace = true
}

tasks.getByName<CargoPublish>("publish") {
    //Publishing Configuration (Task-level)
    dryRun = true
    target = "armeabi-unknown-linux-gnu"
    token = "TOKEN"
}

tasks.getByName<CargoTest>("test") {
    //Testing Configuration (Task-level)
    target = "armeabi-unknown-linux-gnu"
    testThreads = 2
}
```

**Last Updated:** 0.1.0

* Global-level Configurations are automatically assigned to Top-level and then Task-level Configurations.
* Top-level Configurations override Global-level Configurations and are automatically assigned to Task-level Configurations.
* Task-level Configurations override both Global-level and Top-level Configurations above it.

Neo Rust Gradle Plugin was designed this way to allow fine-level control over the command options where developers need
it.

## Requirements
This has not been concretely determined. Please try it out and tell me if it works or not!

## Contributing Steps
1. Fork the Repository.
2. Write your code in that forked repository. (Make sure it follows the structure of the source code.)
3. Test and make sure it works.
4. Create an issue detailing the feature added or problem you fixed.
5. Create a pull request mentioning the issue number.
6. Wait for a maintainer to accept or request additional changes.
7. Voila!

**From Ron: _I'm looking for three active maintainers to look over the project. If you love this project, please find me on
Linkedin or something. It's connected to my Github profile._**

## License (Apache 2.0)
```text
Copyright 2024 on Lauren Hombre (and the neo-rust-gradle-plugin contributors)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0
       
       and included as LICENSE.txt in this Project.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```