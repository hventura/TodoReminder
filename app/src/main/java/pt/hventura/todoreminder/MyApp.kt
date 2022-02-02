package pt.hventura.todoreminder

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pt.hventura.todoreminder.locationreminders.data.ReminderDataSource
import pt.hventura.todoreminder.locationreminders.data.local.LocalDB
import pt.hventura.todoreminder.locationreminders.data.local.RemindersLocalRepository
import pt.hventura.todoreminder.locationreminders.reminderslist.RemindersListViewModel
import pt.hventura.todoreminder.locationreminders.savereminder.SaveReminderViewModel
import timber.log.Timber

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@MyApp) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }

    }
}