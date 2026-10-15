# Neo Rust Gradle Plugin (0.6.1)

![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/asia.hombre.neorust?style=for-the-badge&label=neo-rust-gradle-plugin)

A Gradle Plugin to build Rust projects using Gradle as the Build Environment instead of natively using Cargo.
Gradle is a powerful build tool and utilizing it for Rust projects brings massive benefits as it reduces the complexity
especially when it comes to projects working with multiple programming languages. Such an example is an Android app that
compiles Rust code to JNI binaries.

This plugin aims to make it seamless to run Rust projects with Gradle while maintaining a familiar environment to Rust
developers.

(Note: This is not a fork of rust-gradle-plugin. This was made and designed from scratch.)

> [!NOTE]
> Due to the complexity of Cargo, this plugin should be considered 'beta' until version 1.0.0. There are still many
> features missing and hasn't been thoroughly tested yet. In the future, I'd like to port large OSS Rust projects into
> Gradle using my plugin as a final compatibility test. Checkout my Rust project utilizing this plugin for more details
> [KeccakRust](https://github.com/ronhombre/KeccakRust).

## Project Versioning
This project follows the [Semantic Versioning 2.0](https://semver.org/) when updating the version number. As it stands,
the project is still under development and the API might change between MINOR versions(that is the **y** in x.y.z).

## Features

| Plugin Task             | Gradle Task                      | Cargo Equivalent | Status | Since | Remarks                   |
|-------------------------|----------------------------------|------------------|:------:|:-----:|:--------------------------|
| `CargoBuild`            | `gradle build`                   | `cargo build`    |  Full  | 0.1.0 | Based on Cargo Docs*      |
| `CargoClean`            | `gradle clean`                   | `cargo clean`    |  Full  | 0.4.0 | Based on Cargo Docs*      |
| `CargoBench`            | `gradle bench`                   | `cargo bench`    |  Full  | 0.1.0 | Based on Cargo Docs*      |
| `CargoTest`             | `gradle test`                    | `cargo test`     |  Full  | 0.1.0 | Based on Cargo Docs*      |
| `CargoPublish`          | `gradle publish`                 | `cargo publish`  |  Full  | 0.1.0 | Based on Cargo Docs*      |
| `CargoManifestGenerate` | `gradle generateCargoManifest`   | N/A              |  Semi  | 0.2.0 | Not all fields**          |
| `CargoDoc`              | `gradle rustdoc`                 | `cargo doc`      |  WIP   |  N/A  | Will be implemented soon. |
| `CargoReport`           | `gradle report`                  | `cargo report`   |  TBD   |  N/A  | Not prioritized.          |
| `CargoFix`              | `gradle fix`                     | `cargo fix`      |  TBD   |  N/A  | Not prioritized.          |

_*_ Based on [Cargo Docs](https://doc.rust-lang.org/cargo/commands).

_**_ Based on [Cargo Manifest Docs](https://doc.rust-lang.org/cargo/reference/manifest.html). The implemented features
are limited to the whole `package` section(except for `resolver` field), the whole dependency tables(except for `target`
because of its complexity), and limited support for `lib` section.

> [!NOTE]
> Due to the new DSL, some of the features may not work as expected.

| Status  | Definition                                            |
|---------|-------------------------------------------------------|
| Full    | Fully supported all command options.                  |
| Semi    | Limited support that might not allow command options. |
| WIP     | It is being implemented at the moment.                |
| TBD     | Planned to be done in the future.                     |
| No Plan | Avoided implementing due to design or logic reasons.  |

### Current Status
As of version 0.6.1, Neo Rust Gradle Plugin can build, test, benchmark, publish(?), and run most Rust projects without
requiring a Cargo.toml file. Additionally, users can use local Gradle modules as crate dependencies

**Key capabilities:**
- Full support for basic Cargo commands (build, test, bench, publish(?))
- Gradle-style dependency management for Rust crates
- Custom Cargo.toml manifest generation.
- Crate features support
- Run built binaries directly from Gradle
- Build only the library or the binary you want
- Idiomatic crate dependency declaration
- Latest Gradle 9.8.1 support
- Gradle Parallel Build support (allow tasks to run in different threads at the same time)
- Gradle Configuration Cache support (faster task run)
- Gradle Incremental Build support (only runs build when source files are updated, including dependency source files)
- Sub-module support (Declare Gradle projects as crate dependency)
- Task wiring for projects and sub-projects
- Custom Gradle DSL for Rust projects

We're actively working on expanding support for more complex Rust project structures and additional Cargo features. If
you need a feature now, consider contributing or submitting an issue!

## Project Structure (Similar to Java and Kotlin)
```text
project-root/
├── build.gradle.kts        # Gradle build file where you configure the plugin
├── settings.gradle.kts     # Gradle settings file
└── src/
    └── main/
        └── rust/
            └── main.rs     # Your main binary Rust file (location configurable)
            └── other       # Other binary Rust file (location configurable)
                └── main.rs # Main binary Rust file for 'other'
            └── helpers     # Rust module folder for 'helpers' (location configurable)
                └── mod.rs
            └── webhelp.rs  # Rust module file for 'webhelp' (location configurable)
    └── library/
        └── rust/
            └── lib.rs      # Your main library Rust file (location configurable)
    └── test/
        └── rust/
            └── main.rs     # Your main test Rust file (location configurable)
    └── bench/
        └── rust/
            └── main.rs     # Your main bench Rust file (location configurable)
```
### Installation
`build.gradle.kts`
```kotlin
plugins {
    id("asia.hombre.neorust") version "0.6.1"
}
```

## Sample Code
### Rust Projects Using This Plugin
* [KeccakRust](https://github.com/ronhombre/KeccakRust)
### Gradle Kotlin Sample DSL (build.gradle.kts)
```kotlin
import asia.hombre.neorust.option.PanicFlag

plugins {
    id("asia.hombre.neorust") version "0.6.1"
}

group = "asia.hombre.keccak"
version = "0.0.7"

repositories {
    mavenCentral()
}

dependencies {
    crate("zeroize:1.8.2")
    crate("hex:0.4.3") {
        optional = true
    }
    devCrate("hex:0.4.3")
    devCrate("criterion:0.8.2")
}

rust {
    authors.add("Ron Lauren Hombre <ronlauren@hombre.asia>")
    edition = "2024"

    library {
        crateType.add("dylib")
        crateType.add("rlib") //We are adding this to avoid double linking when running our binary executables
    }

    features {
        feature("default", "standalone")
        feature("standalone")
        feature("executable", "dep:hex")
    }

    profiles {
        val dev by getting {
            panic = PanicFlag.abort
        }
        val release by getting {
            panic = PanicFlag.abort
        }
    }

    binaries {
        val main by getting {
            noDefaultFeatures = true
            buildFeatures.add("executable")
        }
    }

    tests {
        val main by getting {
            environment.put("RUST_BACKTRACE", "full")
            buildFeatures.add("executable")
        }
    }

    benchmarks {
        val standard by getting {
            buildFeatures.add("executable")
        }
    }
}
```

See the complete [DSL](https://github.com/ronhombre/KeccakRust/blob/master/DSL.md) for more details.

* Global-level Configurations are automatically assigned to Top-level and then Task-level Configurations.
* Top-level Configurations override Global-level Configurations and are automatically assigned to Task-level Configurations.
* Task-level Configurations override both Global-level and Top-level Configurations above it.

Neo Rust Gradle Plugin was designed this way to allow fine-level control over the command options where developers need
it.

## Building Requirements
* Cargo (and its dependencies)
* Gradle
* JDK 17.0+ (8 if you're simply using the plugin)

These haven't been concretely determined. Please try it out and tell me if it works or not!

## Contributing Steps
1. Fork the Repository.
2. Write your code in that forked repository. (Make sure it follows the structure of the source code.)
3. Test and make sure it works.
4. Create an issue detailing the feature added or problem you fixed.
5. Create a pull request mentioning the issue number.
6. Wait for a maintainer to accept or request additional changes.
7. Voila!

Hire me at [Linkedin](https://www.linkedin.com/in/rlhombre/) if you want exclusive development on this project

## Other Resources
- [Support Discord Server](https://discord.gg/fwASv6XDXp)

## License (Apache 2.0)
```text
Copyright 2026 Ron Lauren Hombre (and the neo-rust-gradle-plugin contributors)

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
