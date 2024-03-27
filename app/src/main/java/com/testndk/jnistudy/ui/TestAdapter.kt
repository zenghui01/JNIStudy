//package com.testndk.jnistudy.ui
//
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentStatePagerAdapter
//
//
//class TestAdapter(fm: FragmentManager, val datas: MutableList<String>) :
//    FragmentStatePagerAdapter(fm) {
//    private val mFragments = hashMapOf<Int, Fragment>()
//    override fun getCount(): Int {
//        return datas.size
//    }
//
//    override fun getItem(position: Int): Fragment {
//        return mFragments.getOrPut(position) {
//            Fragment().apply {
//                // setData(datas)
//            }
//        }
//    }
//}