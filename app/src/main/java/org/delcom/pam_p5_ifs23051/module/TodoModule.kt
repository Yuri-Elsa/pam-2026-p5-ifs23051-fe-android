package org.delcom.pam_p5_ifs23051.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.delcom.pam_p5_ifs23051.network.todos.service.ITodoAppContainer
import org.delcom.pam_p5_ifs23051.network.todos.service.ITodoRepository
import org.delcom.pam_p5_ifs23051.network.todos.service.TodoAppContainer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TodoModule {

    // ✅ FIX: Tambahkan @Singleton agar instance OkHttpClient + Retrofit hanya dibuat SEKALI
    // Tanpa @Singleton, Hilt membuat instance baru setiap kali dependency dibutuhkan.
    // OkHttpClient dan Retrofit adalah objek berat (thread pool, connection pool, dll)
    // yang sangat mahal bila dibuat berulang → app lambat & boros memori.
    @Provides
    @Singleton
    fun provideTodoAppContainer(): ITodoAppContainer {
        return TodoAppContainer()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(container: ITodoAppContainer): ITodoRepository {
        return container.repository
    }
}