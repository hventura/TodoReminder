package pt.hventura.todoreminder.locationreminders.reminderslist

import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseRecyclerViewAdapter

//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.item_reminder
}