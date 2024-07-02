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

| Plugin Task             | Gradle Task                      | Cargo Equivalent | Status | Since | Remarks                                    |
|-------------------------|----------------------------------|------------------|:------:|:-----:|:-------------------------------------------|
| `CargoBuild`            | `gradle build`                   | `cargo build`    |  Full  | 0.1.0 | Based on Cargo Docs*                       |
| N/A                     | `gradle clean`                   | `cargo clean`    | BROKEN | 0.2.0 | Can't find the generated Cargo.toml**      |
| `CargoBench`            | `gradle bench`                   | `cargo bench`    |  Full  | 0.1.0 | Based on Cargo Docs*                       |
| `CargoTest`             | `gradle test`                    | `cargo test`     |  Full  | 0.1.0 | Based on Cargo Docs*                       |
| `CargoPublish`          | `gradle publish`                 | `cargo publish`  |  Full  | 0.1.0 | Based on Cargo Docs*                       |
| `CargoResolver`         | `gradle resolveRustDependencies` | N/A              |  Full  | 0.2.0 | Compiles Gradle defined rust dependencies. |
| `CargoManifestGenerate` | `gradle generateCargoManifest`   | N/A              |  Semi  | 0.2.0 | Not all fields***                          |
| `CargoDoc`              | `gradle rustdoc`                 | `cargo doc`      |  WIP   |  N/A  | Implementing this will break the codebase. |
| `CargoReport`           | `gradle report`                  | `cargo report`   |  TBD   |  N/A  | Not prioritized.                           |
| `CargoFix`              | `gradle fix`                     | `cargo fix`      |  TBD   |  N/A  | Not prioritized.                           |

_*_ Based on [Cargo Docs](https://doc.rust-lang.org/cargo/commands).

_**_ It executes a simple `cargo clean` command and isn't fed any information about the location of the generated Cargo.toml.

_***_ Based on [Cargo Manifest Docs](https://doc.rust-lang.org/cargo/reference/manifest.html). The implemented features
are limited to the whole `package` section(except for `resolver` field), the whole dependency tables(except for `target`
because of its complexity), and limited support for `lib` section.

| Status  | Definition                                            |
|---------|-------------------------------------------------------|
| Full    | Fully supported all command options.                  |
| Semi    | Limited support that might not allow command options. |
| WIP     | It is being implemented at the moment.                |
| TBD     | Planned to be done in the future.                     |
| No Plan | Avoided implementing due to design or logic reasons.  |

### Current Status
As of version 0.2.0, Neo Rust Gradle Plugin can build, test, benchmark, and publish(?) simple Rust projects without
requiring a Cargo.toml file. It generates the necessary Cargo.toml during the build process based on your Gradle
configuration.

**Key capabilities:**
- Full support for basic Cargo commands (build, test, bench, publish).
- Gradle-style dependency management for Rust crates.
- Custom Cargo.toml manifest generation.

We're actively working on expanding support for more complex Rust project structures and additional Cargo features. If
you need a feature now, consider contributing!

## Project Structure (Similar to Java and Kotlin)
```text
project-root/
├── build.gradle.kts    # Gradle build file where you configure the plugin
├── settings.gradle.kts # Gradle settings file
└── src/
    └── main/
        └── rust/
            └── lib.rs  # Your main Rust file (location configurable)
```

## Sample Code
### Gradle Kotlin DSL (build.gradle.kts)
```kotlin
import asia.hombre.neorust.task.CargoBench
import asia.hombre.neorust.task.CargoBuild
import asia.hombre.neorust.task.CargoPublish
import asia.hombre.neorust.task.CargoTest

plugins {
    id("asia.hombre.neorust") version "0.2.0"
}

dependencies {
    implementation("crates.io:jni:0.21.1") //Becomes rustImplementation if there are conflicting plugins.
    devOnly("crates.io:jni:0.21.1") //Becomes rustDevOnly if there are conflicting plugins.
    buildOnly("crates.io:jni:0.21.1") //Becomes rustBuildOnly if there are conflicting plugins.
}

//Rust Configuration (Global-level)
rust {
    target = "x86-unknown-linux-gnu"
    
    //This replaces the Cargo.toml and generates it in the ./build/ folder.
    manifest {
        packaging {
            name = "neorust"
            authors.add("Ron Lauren Hombre <email@example.com>")
            edition = "2021"
        }
        lib {
            crateType.add("cdylib")
        }
    }

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

tasks.getByName<CargoBench>("bench") { //Becomes benchRust if conflicting a task name.
    //Benchmarking Configuration (Task-level)
    target = "armeabi-unknown-linux-gnu"
    noRun = true
}

tasks.getByName<CargoBuild>("build") { //Becomes buildRust if conflicting a task name.
    //Building Configuration (Task-level)
    target = "armeabi-unknown-linux-gnu"
    workspace = true
}

tasks.getByName<CargoPublish>("publish") { //Becomes publishRust if conflicting a task name.
    //Publishing Configuration (Task-level)
    dryRun = true
    target = "armeabi-unknown-linux-gnu"
    token = "TOKEN"
}

tasks.getByName<CargoTest>("test") { //Becomes testRust if conflicting a task name.
    //Testing Configuration (Task-level)
    target = "armeabi-unknown-linux-gnu"
    testThreads = 2
}
```

**Last Updated:** 0.2.0

* Global-level Configurations are automatically assigned to Top-level and then Task-level Configurations.
* Top-level Configurations override Global-level Configurations and are automatically assigned to Task-level Configurations.
* Task-level Configurations override both Global-level and Top-level Configurations above it.

Neo Rust Gradle Plugin was designed this way to allow fine-level control over the command options where developers need
it.

## Requirements
* Cargo (and its dependencies)
* Gradle
* JDK 8.0+

These haven't been concretely determined. Please try it out and tell me if it works or not!

## Contributing Steps
1. Fork the Repository.
2. Write your code in that forked repository. (Make sure it follows the structure of the source code.)
3. Test and make sure it works.
4. Create an issue detailing the feature added or problem you fixed.
5. Create a pull request mentioning the issue number.
6. Wait for a maintainer to accept or request additional changes.
7. Voila!

**From Ron: _I'm looking for three active maintainers to look over the project. If you love this project, please find me on
Linkedin connected to my Github Profile or the Discord Server below._**

## Other Resources
- [Support Discord Server](https://discord.gg/fwASv6XDXp)

## License (Apache 2.0)
```text
Copyright 2024 Ron Lauren Hombre (and the neo-rust-gradle-plugin contributors)

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