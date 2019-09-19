/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.publishing.maven

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class MavenBasePublishPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.apply org.gradle.api.publish.maven.plugins.MavenPublishPlugin

        project.publishing {
            publications {
                withType(MavenPublication) {
                    if (! project.state.executed) {
                        project.afterEvaluate { p ->
                            configureDescription(it, p)
                        }
                    } else {
                        configureDescription(it, project)
                    }
                }
            }
        }
    }

    private void configureDescription(MavenPublication publication, Project p) {
        publication.pom {
            name = p.name
            description = p.description
        }
    }
}
