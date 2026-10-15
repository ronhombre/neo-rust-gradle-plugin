```kotlin
dependencies {
    crate("package-name:0.0.1")
    crate("packager:0.0.2") {
        defaultFeatures = false
        optional = true
    }
}
rust {
    authors.add("John Doe <johndoe@example.com>")
    edition = "2024"
    rustVersion = "1.90"
    documentation = "https://example.com/documentation"
    //Optional: Automatically detected
    readme = File("README.md")
    homepage = "https://example.com/home"
    repository = "https://example.com/repository"
    license = "Apache-2.0"
    //Optional: Automatically detected
    licenseFile = File("LICENSE.md")
    keywords.addAll("example", "nrgp")
    categories.addAll("api-bindings", "development-tools")
    //Optional: Automatically detected for root projects and sub-projects
    workspace = File("./")
    //Optional: Automatically detected on project directory
    buildFile = File("build.rs")
    links = "git2"
    exclude.addAll("examples/*", "*.yaml")
    include.addAll("tests/*", "DSL.md")
    publish.addAll("some-registry", "your-registry")
    publishEnabled = false //Overrides publish and disables publishing
    defaultRun = "binary-2"
    
    metadata {
        val android by registering {
            //Every input here is escaped
            string("package-name", "nrgp-built-app")
            string("assets", "path/to/static")
            boolean("key", false)
            number("key", 0)
            number("key", 0f)
            number("key", 0.2)
            number("key", 0L)
        }
    }
    
    lints {
        val rust by getting {
            //TODO: Not supported yet.
        }
        val clippy by getting {
            //TODO: Not supported yet.
        }
    }
    
    profiles {
        //Overrides cannot specify the panic, lto, or rpath settings.
        val dev by getting {
            optLevel
            debug
            splitDebugInfo
            strip
            debugAssertions
            overflowChecks
            lto
            panic
            incremental
            codegenUnits
            rpath
            
            buildOverride {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }
            
            packageOverride("package") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("*") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }
        }
        val release by getting {
            optLevel
            debug
            splitDebugInfo
            strip
            debugAssertions
            overflowChecks
            lto
            panic
            incremental
            codegenUnits
            rpath

            buildOverride {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("package") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("*") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }
        }
        val test by getting {
            optLevel
            debug
            splitDebugInfo
            strip
            debugAssertions
            overflowChecks
            lto
            panic
            incremental
            codegenUnits
            rpath

            buildOverride {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("package") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("*") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }
        }
        val bench by getting {
            optLevel
            debug
            splitDebugInfo
            strip
            debugAssertions
            overflowChecks
            lto
            panic
            incremental
            codegenUnits
            rpath

            buildOverride {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("package") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("*") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }
        }
        //Register a custom profile; 'custom' can be replaced with any text
        val custom by registering {
            optLevel
            debug
            splitDebugInfo
            strip
            debugAssertions
            overflowChecks
            lto
            panic
            incremental
            codegenUnits
            rpath

            buildOverride {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("package") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }

            packageOverride("*") {
                optLevel
                debug
                splitDebugInfo
                strip
                debugAssertions
                overflowChecks
                lto
                panic
                incremental
                codegenUnits
                rpath
            }
        }
    }
    
    features {
        feature("default", "standalone", "example")
        feature("standalone")
        feature("executable", "dep:hex")
        feature("example")
    }

    binaries {
        val main by getting {
            test = false
        }
        //Uses the same entry .rs file as main
        val mainWithArgument by inheriting(main) {
            //You can then specify different arguments or configurations here without affecting main
        }
    }

    tests {
        val regression by getting {
            buildFeatures.add("executable")
        }
    }

    benchmarks {
        val standard by getting {
            harness = false
            buildFeatures.add("executable")

        }
    }

    examples {
        toolchain = "wow"
        val mainexample by getting {
            test = false
        }
    }
}
```