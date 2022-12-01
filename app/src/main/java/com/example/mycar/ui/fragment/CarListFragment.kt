package com.example.mycar.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycar.BaseApplication
import com.example.mycar.R
import com.example.mycar.databinding.FragmentCarListBinding
import com.example.mycar.network.logo.MyCarLogo
import com.example.mycar.ui.adapter.CarListAdapter
import com.example.mycar.ui.viewmodel.CarViewModel
import com.example.mycar.ui.viewmodel.CarViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [CarListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarListFragment : Fragment() {

    private val viewModel: CarViewModel by activityViewModels {
        CarViewModelFactory(
            (activity?.application as BaseApplication).database.myCarDao()
        )
    }

    private var _binding: FragmentCarListBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        try {
            // Inflate the layout for this fragment
            _binding = FragmentCarListBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e("ErrorList", "onCreateViewCarListFragment", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val adapter =
                CarListAdapter(logoDataApi = viewModel.logoDataApi, clickListener = { car ->
                    val action = CarListFragmentDirections
                        .actionCarListFragmentToCarDetailFragment(
                            car.id
                        )
                    findNavController().navigate(action)
                })

            val observer = Observer<List<MyCarLogo>> {
                binding.recyclerView.adapter = adapter
            }
            viewModel.logoDataApi.observe(viewLifecycleOwner, observer)
            viewModel.allCar.observe(this.viewLifecycleOwner) { cars ->
                cars.let {
                    adapter.submitList(it)
                }
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
            binding.recyclerView.adapter = adapter
            binding.addCar.setOnClickListener {
                val addAction = CarListFragmentDirections.actionCarListFragmentToAddCarFragment(
                    id = 0,
                    getString(R.string.add_car)
                )
                this.findNavController().navigate(
                    addAction
                )
            }
        } catch (e: Exception) {
            Log.e("ErrorList", "onViewCreateCarListFragment", e)
            throw e
        }
    }

}