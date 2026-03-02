package org.delcom.pam_p5_ifs23051.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.delcom.pam_p5_ifs23051.network.todos.service.ITodoAppContainer
import org.delcom.pam_p5_ifs23051.network.todos.service.ITodoRepository
import org.delcom.pam_p5_ifs23051.network.todos.service.TodoAppContainer

@Module
@InstallIn(SingletonComponent::class)
object TodoModule {
    @Provides
    fun providePlantContainer(): ITodoAppContainer {
        return TodoAppContainer()
    }

    @Provides
    fun providePlantRepository(container: ITodoAppContainer): ITodoRepository {
        return container.repository
    }
}