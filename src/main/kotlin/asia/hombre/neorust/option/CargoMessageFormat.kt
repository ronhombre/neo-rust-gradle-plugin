/*
 * Copyright 2025 Ron Lauren Hombre (and the neo-rust-gradle-plugin contributors)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * and included as LICENSE.txt in this Project.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asia.hombre.neorust.option

/**
 * Command output format for Cargo
 *
 * @since 0.1.0
 * @author Ron Lauren Hombre
 */
enum class CargoMessageFormat {
    /**
     * Displays messages in a human-readable text format. This is the default.
     */
    human,

    /**
     * Emits shorter, human-readable text messages.
     */
    short,

    /**
     * Emits JSON messages to stdout.
     */
    json,

    /**
     * Ensures the rendered field of JSON messages contains the "short" rendering from rustc.
     */
    json_diagnostic_short,

    /**
     * Ensures the rendered field of JSON messages contains embedded ANSI color codes for respecting rustc's default color scheme.
     */
    json_diagnostic_rendered_ansi,

    /**
     * Instructs Cargo to not include rustc diagnostics in JSON messages printed, but instead Cargo itself should render the JSON diagnostics coming from rustc.
     */
    json_render_diagnostics
}