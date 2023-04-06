package com.example.projet_smartphone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class MyAdapter (private val list:ArrayList<String>, private val context: Context): BaseAdapter() {
    //retourne la taile de la liste
    override fun getCount(): Int {
        return list.size
    }

    //retourne item a la position p0
    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    //retourne id de item a la postion p0
    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    //creation de item en fonction de chaque trajectoire de la liste
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutItem: ConstraintLayout?
        val mInflater: LayoutInflater = LayoutInflater.from(context)

        //recuperation du layout de item
        layoutItem = if(convertView == null) {
            mInflater.inflate(R.layout.item_layout, parent, false) as ConstraintLayout
        }else{
            convertView as ConstraintLayout
        }

        var viewHolder: ViewHolder? = layoutItem.tag as? ViewHolder
        if(viewHolder == null){
            viewHolder = ViewHolder()
            //attribution des id des composants de la vue à la structure
            viewHolder.tv = layoutItem.findViewById(R.id.text_view)
            viewHolder.but = layoutItem.findViewById(R.id.button)
            layoutItem.tag = viewHolder
        }

        //recuperation du texte courant
        val trajCourant:String = list[position]

        //attribution du texte au textview de l'item
        viewHolder.tv?.text = trajCourant

        //attribution de id au bouton de l'item
        viewHolder.but?.text = (position + 1).toString()

        //retour du layout de item
        return layoutItem
    }

    //structure de item donc bouton et textview : ref item_layout.xml
    private class ViewHolder(var tv: TextView?, var but: Button?){
        constructor():this(
            tv=null,
            but=null
        )
    }

}