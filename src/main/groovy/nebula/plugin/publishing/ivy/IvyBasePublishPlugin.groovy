/*
 * Copyright 2015-2017 Netflix, Inc.
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
package nebula.plugin.publishing.ivy
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.publish.ivy.IvyPublication

class IvyBasePublishPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.apply org.gradle.api.publish.ivy.plugins.IvyPublishPlugin

        // CURRENT
        /*
            <conf name="default" visibility="public" extends="runtime"/>
            <conf visibility="public" name="runtime"/>
            <conf visibility="public" name="optional"/>
            <conf visibility="public" name="provided"/>
         */

        // EXPECTED
        /*
            <conf name="compile" visibility="public"/>
            <conf name="default" visibility="public" extends="runtime,master"/>
            <conf name="javadoc" visibility="public"/>
            <conf name="master" visibility="public"/>
            <conf name="resources" visibility="public"/>
            <conf name="runtime" visibility="public" extends="compile"/>
            <conf name="sources" visibility="public"/>
            <conf name="system" visibility="public"/>
            <conf name="test" visibility="public" extends="runtime"/>
            <conf name="optional" visibility="public"/>
            <conf name="provided" visibility="public"/>
         */

        project.publishing {
            publications {
                withType(IvyPublication) {
                    if (! project.state.executed) {
                        project.afterEvaluate { p ->
                            configureDescription(it, p)
                        }
                    } else {
                        configureDescription(it, project)
                    }

                    descriptor.withXml { XmlProvider xml ->
                        def root = xml.asNode()
                        def configurationsNode = root?.configurations
                        if(!configurationsNode) {
                            configurationsNode = root.appendNode('configurations')
                        }
                        else {
                            configurationsNode = configurationsNode[0]
                        }

                        def minimalConfs = [
                            compile: [], default: ['runtime', 'master'], javadoc: [], master: [],
                            runtime: ['compile'], sources: [], test: ['runtime']
                        ]

                        minimalConfs.each { minimal ->
                            def conf = configurationsNode.conf.find { it.@name == minimal.key }
                            if(!conf) {
                                conf = configurationsNode.appendNode('conf')
                            }
                            conf.@name = minimal.key
                            conf.@visibility = 'public'

                            if(!minimal.value.empty)
                                conf.@extends = minimal.value.join(',')
                        }
                    }
                }
            }
        }
    }

    private void configureDescription(IvyPublication publication, Project p) {
        publication.descriptor.status = p.status
        publication.descriptor.description {
            text = p.description ?: ''
        }
    }
}
