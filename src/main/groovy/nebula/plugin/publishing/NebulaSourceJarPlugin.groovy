package nebula.plugin.publishing

import nebula.plugin.publishing.component.CustomComponentPlugin
import nebula.plugin.publishing.maven.NebulaBaseMavenPublishingPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

class NebulaSourceJarPlugin implements Plugin<Project>{
    private static Logger logger = Logging.getLogger(NebulaSourceJarPlugin);

    protected Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.plugins.withType(JavaPlugin) {
            // TODO Look at multiple sourceSets, is groovy another sourceSet?
            def sourceJar = project.tasks.create([name: 'sourceJar', type: Jar]) {
                dependsOn project.tasks.getByName('classes')
                from project.sourceSets.main.allSource
                classifier 'sources'
                extension 'jar'
                group 'build'
            }

            def sourcesConf = project.configurations.create('sources')
            project.configurations.getByName(Dependency.ARCHIVES_CONFIGURATION).extendsFrom(sourcesConf)

            CustomComponentPlugin.addArtifact(project, sourcesConf.name, sourceJar, 'sources')

            project.plugins.withType(NebulaBaseMavenPublishingPlugin) {
                it.withMavenPublication { mavenPub ->
                    mavenPub.artifact(sourceJar)
                }
            }
        }
    }
}
