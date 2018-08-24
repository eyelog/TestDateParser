package ru.eyelog.testdateparser

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_list.view.*
import java.util.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val generatedData = dateGenerator()

        rv_list.layoutManager = LinearLayoutManager(this)

        rv_list.adapter = ListAdapter(generatedData, this)

    }

    fun dateGenerator(): ArrayList<HashMap<String, String>> {

        var genStringDate: String? = null
        var genStringValue: String? = null
        val period: Int = 7
        var calendar: Calendar? = null
        var random: Random? = null

        calendar = Calendar.getInstance()
        random = Random()

        val valueList = arrayListOf<kotlin.collections.HashMap<String, String>>()

        // Генерируем данные
        for (i in 0..period) {
            calendar!!.add(Calendar.DAY_OF_MONTH, -1)

            val sb = StringBuilder()
            sb.append(calendar.get(Calendar.YEAR)).append("-")
                    .append(calendar.get(Calendar.MONTH)).append("-")
                    .append(calendar.get(Calendar.DAY_OF_MONTH)).append(" (")
                    .append(calendar.get(Calendar.HOUR_OF_DAY)).append(":")
                    .append(calendar.get(Calendar.MINUTE)).append(")")
            genStringDate = sb.toString()

            val ri  = random.nextInt(200)+100
            genStringValue = (ri).toString()

            val map = hashMapOf("date" to genStringDate, "value" to genStringValue)
            valueList.add(map)
        }

        // В конце добавим повтор и пару пропусков
        for (i in 0..3) {
            calendar!!.add(Calendar.DAY_OF_MONTH, -i)

            val sb = StringBuilder()
            sb.append(calendar.get(Calendar.YEAR)).append("-")
                    .append(calendar.get(Calendar.MONTH)).append("-")
                    .append(calendar.get(Calendar.DAY_OF_MONTH)).append(" (")
                    .append(calendar.get(Calendar.HOUR_OF_DAY)).append(":")
                    .append(calendar.get(Calendar.MINUTE)).append(")")
            genStringDate = sb.toString()

            val ri  = random.nextInt(200)+100
            genStringValue = (ri).toString()

            val map = hashMapOf("date" to genStringDate, "value" to genStringValue)
            valueList.add(map)
        }

        return valueList

    }

    class ListAdapter(val items: ArrayList<HashMap<String, String>>?, val context: Context):
            RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        override fun getItemCount(): Int {
            return items!!.size
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list, p0, false))
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            p0.itemView.tv_list_date.text = items!!.get(p1).get("date")
            p0.itemView.tv_list_value.text = items.get(p1).get("value")
        }
    }

    class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val tvListDate = view.tv_list_date
        val tvListValue = view.tv_list_value
    }



    // Данная функция убирает часы из даты и усредняет данные для дублируемых дат
    // Даллее собирает календарный период, если есть пропуски по датам - дублирует предыдущие даты
    // И в зависимости от заданного параметра собирает пользовательский период
    // Параметры запроса:
    // 0 - Последние 7 дней
    // 1 - Текущий месяц
    // 2 - Последние 30 дней
    // 3 - Последние 180 дней
    // 4 - Текущий год
    // 5 - Весь срок
    // 6 - Заданный период

    fun dateParser(listDate: ArrayList<String>, listValues: ArrayList<String>, period: Int): ArrayList<HashMap<String, String>> {

        // Сначала убираем часы из даты и усредняем данные для дублируемых дат
        val parsedList = arrayListOf<kotlin.collections.HashMap<String, String>>()

        var i = 0
        var map = hashMapOf("date" to listDate[i].substring(0, 10), "value" to listValues[i])
        parsedList.add(map)

        do {

            i++

            if(i < listDate.size){
                if (parsedList[parsedList.lastIndex]["date"] == listDate[i].substring(0, 10)) {

                    val middleValue = ((parsedList[parsedList.lastIndex]["value"]?.toInt()?.plus(listValues[i].toInt()))?.div(2)).toString()
                    map = hashMapOf("date" to listDate[i].substring(0, 10), "value" to middleValue)
                    parsedList[parsedList.lastIndex] = map

                } else {
                    map = hashMapOf("date" to listDate[i].substring(0, 10), "value" to listValues[i])
                    parsedList.add(map)

                }
            }

        }
        while (i < listDate.size)

        // Далее собираем полный календарь.
        var calendarList = arrayListOf<CalendarObject>()
        var calendar: Calendar? = null
        for(calObj in parsedList){
            calendar?.set(calObj.get("date")?.substring(0, 4)!!.toInt(),    // Год
                    calObj.get("date")?.substring(5, 7)!!.toInt(),          // Месяц
                    calObj.get("date")?.substring(8, 10)!!.toInt())         // Число
            var calendarObject = CalendarObject(calendar!!, calObj.get("value")!!.toInt())
            calendarList.add(calendarObject)
        }

        // Даллее выявляемм пропущенные даты и заполняем соответствующие данные
        for (outLine in calendarList){
            Log.e("outLine", outLine.toString())
        }


        return parsedList
    }

    // Кастомный объект для списка
    class CalendarObject (calendar: Calendar, dateValue: Int){
        val calendarOdj: Calendar = calendar
        val dateValueOdj = dateValue
    }
}

