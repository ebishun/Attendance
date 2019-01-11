package promosys.com.promosysattendance

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView


class AttendanceAdapter(private val lstAttendance: ArrayList<AttendanceObject>) : RecyclerView.Adapter<AttendanceAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val context: Context = view.context
        var activityMain: MainActivity? = null
        init {
            activityMain = context as MainActivity
        }

        val txtEmployee: TextView = view.findViewById(R.id.txt_employee_name)
        val txtTime: TextView = view.findViewById(R.id.txt_timestamp)
        val imgNotifyIcon: ImageView = view.findViewById(R.id.imgStatusIcon)
        val layoutImg:RelativeLayout = view.findViewById(R.id.relative_layout_2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val attendanceObject = lstAttendance[position]
        holder.txtEmployee.text = attendanceObject.employeeName
        holder.txtTime.text = attendanceObject.employeeTime

        if(attendanceObject.employeeStatus == 0){
            holder.txtEmployee.setBackgroundResource(R.color.colorDefault)
            holder.txtTime.setBackgroundResource(R.color.colorDefault)
            holder.layoutImg.setBackgroundResource(R.color.colorDefault_light)
            holder.imgNotifyIcon.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp)

        }else if(attendanceObject.employeeStatus == 1){
            holder.txtEmployee.setBackgroundResource(R.color.colorPresent)
            holder.txtTime.setBackgroundResource(R.color.colorPresent)
            holder.layoutImg.setBackgroundResource(R.color.colorPresent_light)
            holder.imgNotifyIcon.setImageResource(R.drawable.ic_check_circle_black_24dp)

        }else if(attendanceObject.employeeStatus == 2 || attendanceObject.employeeStatus == 3){
            holder.txtEmployee.setBackgroundResource(R.color.colorAbsent)
            holder.txtTime.setBackgroundResource(R.color.colorAbsent)
            holder.layoutImg.setBackgroundResource(R.color.colorAbsent_light)
            holder.imgNotifyIcon.setImageResource(R.drawable.ic_cancel_black_24dp)
        }

        holder.itemView.setOnClickListener {
            holder.activityMain!!.fromAttendanceAdapter(position)
        }

        holder.itemView.setOnLongClickListener {
            holder.activityMain!!.fromAttendanceAdapterLongPressed(position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return lstAttendance.size
    }

}