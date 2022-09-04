package com.yusufyildiz.livenessdetection.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.yusufyildiz.livenessdetection.R
import com.yusufyildiz.livenessdetection.databinding.FragmentIntroBinding
import java.util.*

class IntroFragment : Fragment() {

    private lateinit var binding : FragmentIntroBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentIntroBinding.inflate(inflater,container,false)
        val view = binding.root
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = this.activity?.getSharedPreferences("com.yusufyildiz.livenessdetection.view",Context.MODE_PRIVATE) ?: return




        val timer = object : CountDownTimer(2000,1000){
            override fun onTick(p0: Long)  {

                if(sharedPreferences.getBoolean("message",false) == false)
                {
                    Toast.makeText(requireContext(),"Lütfen izinleri verdikten sonra yeniden başlatın !!!",Toast.LENGTH_LONG).show()
                    with(sharedPreferences.edit())
                    {
                        putBoolean("message",true)
                        apply()
                    }

                }
                else
                {
                    with(sharedPreferences.edit())
                    {
                        putBoolean("message",true)
                        apply()
                    }
                }

            }

            override fun onFinish() {

                val action = IntroFragmentDirections.actionIntroFragmentToLivenessFragment()
                findNavController().navigate(action)

            }

        }
        timer.start()
    }

    

}