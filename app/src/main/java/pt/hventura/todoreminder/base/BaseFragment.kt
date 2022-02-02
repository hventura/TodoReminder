package pt.hventura.todoreminder.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment : Fragment() {

    abstract val mViewModel: BaseViewModel

    override fun onStart() {
        super.onStart()
        mViewModel.showErrorMessage.observe(this.viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        mViewModel.showToast.observe(this.viewLifecycleOwner) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        mViewModel.showSnackBar.observe(this.viewLifecycleOwner) {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        }
        mViewModel.showSnackBarInt.observe(this.viewLifecycleOwner) {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        }

        mViewModel.navigationCommand.observe(this.viewLifecycleOwner) { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        }
    }

}