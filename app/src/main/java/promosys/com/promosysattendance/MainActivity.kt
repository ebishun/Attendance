package promosys.com.promosysattendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_main.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.add_employee.*
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.io.FileWriter
import java.io.IOException
import java.util.Arrays

private val CSV_HEADER = "No.,Employee,Attendance"
var fileWriter: FileWriter? = null


class MainActivity : AppCompatActivity() {

    private var employeeList: ArrayList<AttendanceObject>? = null
    private var adapter: AttendanceAdapter?=null
    var gson = Gson()

    var prefs: SharedPreferences?=null
    var editPrefs: SharedPreferences.Editor? = null

    private var jsonString: String?=null
    private val WRITE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()

        prefs = this.getSharedPreferences("PromosysAttendance", 0)
        editPrefs = prefs!!.edit()

        txtDate.text = "Date: " + getDate()

        recvw_employee!!.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        employeeList = ArrayList<AttendanceObject>()
        //val jsonPersonList: String = gson.toJson(employeeList)
        jsonString = prefs!!.getString("EmployeeKey","")
        if(jsonString.isNullOrEmpty()){
            Log.i("MainActivity","Employee Empty")
        }else{
            val itemType = object : TypeToken<ArrayList<AttendanceObject>>() {}.type
            employeeList = gson.fromJson<ArrayList<AttendanceObject>>(jsonString, itemType)
            Log.i("MainActivity","lstSize: " + employeeList!!.size)
        }
        adapter = AttendanceAdapter(employeeList!!)
        recvw_employee!!.adapter = adapter

        btnAddEmployee.setOnClickListener {
            addEmployeeDialog()
        }

        btn_reset.setOnClickListener {
            resetList()
        }

        btnCreateCsv.setOnClickListener {
            createCsvFile()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("MainActivity", "Permission has been denied by user")
                } else {
                    Log.i("MainActivity", "Permission has been granted by user")
                }
            }
        }
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addEmployeeDialog() {
        val context = this
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.add_employee, null)
        builder.setView(view)

        var edtEmployeeName: EditText?=null
        edtEmployeeName = view.findViewById(R.id.edtName)

        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            val attObject = AttendanceObject(edtEmployeeName!!.text.toString(), 0, "Time","Time")
            employeeList!!.add(attObject)
            saveList()
            dialog.cancel()
        }
        builder.show()
    }

    private fun addEmployeeStatus(position:Int) {
        val context = this
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.status_employee, null)
        builder.setView(view)

        var status:Int = 0

        var rbtnPresent: RadioButton?=null
        rbtnPresent = view.findViewById(R.id.rbtnPresent)

        var rbtnLeave: RadioButton?=null
        rbtnLeave = view.findViewById(R.id.rbtnLeave)

        var rbtnMC: RadioButton?=null
        rbtnMC = view.findViewById(R.id.rbtnMC)

        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            if(rbtnPresent.isChecked){
                status = 1
                employeeList!!.get(position).employeeTime = getTime()
            }else if(rbtnLeave.isChecked){
                status = 2
                employeeList!!.get(position).employeeTime = "Leave"
            }else if(rbtnMC.isChecked){
                status = 3
                employeeList!!.get(position).employeeTime = "Klang Office"
            }
            employeeList!!.get(position).employeeStatus = status

            saveList()
            dialog.cancel()
        }
        builder.show()
    }

    private fun editEmployeeStatus(position:Int) {
        val context = this
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.edit_employee, null)
        builder.setView(view)

        var status:Int = 0

        var edtEmployeeName: EditText?=null
        edtEmployeeName = view.findViewById(R.id.edtEmployeeName)

        var edtEmployeeTime: EditText?=null
        edtEmployeeTime = view.findViewById(R.id.edtEntryTime)

        edtEmployeeName.setText(employeeList!!.get(position).employeeName)
        status = employeeList!!.get(position).employeeStatus
        if(status == 1){
            edtEmployeeTime.setVisibility(View.VISIBLE)
            edtEmployeeTime.setText(employeeList!!.get(position).employeeTime)
        }
        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            employeeList!!.get(position).employeeName = edtEmployeeName.text.toString()
            if(status == 1){
                employeeList!!.get(position).employeeTime = edtEmployeeTime.text.toString()
            }
            saveList()
            dialog.cancel()
        }

        builder.setNegativeButton("Delete") { dialog, p1 ->
            employeeList!!.removeAt(position)
            saveList()
            dialog.cancel()
        }


        builder.show()
    }

    private fun resetList(){
        for (i in 0..employeeList!!.size-1){
            employeeList!!.get(i).employeeStatus = 0
            employeeList!!.get(i).employeeTime = "Status"
        }
        saveList()
    }

    private fun saveList(){
        val jsonPersonList: String = gson.toJson(employeeList)
        Log.i("MainActivity","jsonPersonList: " + jsonPersonList)
        editPrefs!!.putString("EmployeeKey",jsonPersonList)
        editPrefs!!.apply()
        adapter!!.notifyDataSetChanged()
    }

    fun fromAttendanceAdapter(position:Int){
        Log.i("NotificationAdapter", "attList: $position")
        addEmployeeStatus(position)
    }

    fun fromAttendanceAdapterLongPressed(position:Int){
        Log.i("NotificationAdapter", "attList: $position")
        editEmployeeStatus(position)
    }

    private fun getTime(): String {
        //val sdf = SimpleDateFormat("MMM dd, hh:mm:ss a")
        val sdf = SimpleDateFormat("hh:mm:ss a")
        var currentDate = sdf.format(Date())
        Log.i("MainActivity","currentTime: $currentDate")
        return currentDate.toString()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("mainActivity", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_REQUEST_CODE)
    }

    private fun getDate(): String {
        val sdf = SimpleDateFormat("EEEE, dd/M/yyyy")
        val currentDate = sdf.format(Date())
        Log.i("MainActivity","currentTime: $currentDate")
        return currentDate.toString()
    }

    private fun getCsvDate(): String {
        val sdf = SimpleDateFormat("dd_M_yyyy")
        val currentDate = sdf.format(Date())
        Log.i("MainActivity","currentTime: $currentDate")
        return currentDate.toString()
    }

    private fun createCsvFile(){
        try {
            var strFileName:String = "/storage/emulated/0/" + "attendance_"+getCsvDate()+".csv"

            //fileWriter = FileWriter("/storage/emulated/0/employee_attendance.csv")
            fileWriter = FileWriter(strFileName)
            var strCsvDate:String = ""
            strCsvDate = "Date: " + getDate()
            fileWriter!!.append(strCsvDate)
            fileWriter!!.append('\n')
            fileWriter!!.append(CSV_HEADER)
            fileWriter!!.append('\n')
            getListToCsv()
        } catch (e: Exception) {
            println("Writing CSV error!")
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter!!.close()
                Log.i("MainActivity","Finish Creating")
            } catch (e: IOException) {
                println("Flushing/closing error!")
                e.printStackTrace()
            }
        }
    }

    private fun getListToCsv(){
        for (i in 0..employeeList!!.size-1){
            var strCsv:String = ""
            strCsv = (i+1).toString() + "," + employeeList!!.get(i).employeeName + "," + employeeList!!.get(i).employeeTime.replace("Time: ","")
            fileWriter!!.append(strCsv)
            fileWriter!!.append('\n')
        }
    }
}
