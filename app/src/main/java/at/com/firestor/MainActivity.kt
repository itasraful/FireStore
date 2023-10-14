package at.com.firestor
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import at.com.firestor.databinding.ActivityMainBinding
import com.google.firebase.Timestamp

class MainActivity : AppCompatActivity(), DataAdapter.ItemClickListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val db = FirebaseFirestore.getInstance()
    private val dataCollection = db.collection("data")
    private val data = mutableListOf<Data>()
    private lateinit var adapter: DataAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        adapter = DataAdapter(data, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addBtn.setOnClickListener {
            val title = binding.titleEtxt.text.toString()
            val description = binding.descEtxt.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                addData(title, description)
            }
        }
        fetchData()

    }

    private fun fetchData() {
        dataCollection.get()
            .addOnSuccessListener {
                data.clear()
                for(document in it){
                    val item = document.toObject(Data::class.java)
                    item.id = document.id
                    data.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data fetched failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addData(title: String, description: String) {
        val newData = Data(title = title, description = description, timestamp = Timestamp.now())
        dataCollection.add(newData)
            .addOnSuccessListener {
                newData.id = it.id
                data.add(newData)
                adapter.notifyDataSetChanged()
                binding.titleEtxt.text?.clear()
                binding.descEtxt.text?.clear()
                Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data added failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onEditItemClick(data: Data) {
        binding.titleEtxt.setText(data.title)
        binding.descEtxt.setText(data.description)
        binding.addBtn.text = "Update"

        binding.addBtn.setOnClickListener {
            val updateTitle = binding.titleEtxt.text.toString()
            val updateDescription = binding.descEtxt.text.toString()
            if(updateTitle.isNotEmpty() && updateDescription.isNotEmpty()){
                val updateData = Data(data.id, updateTitle, updateDescription, Timestamp.now())


                dataCollection.document(data.id!!)
                    .set(updateData)
                    .addOnSuccessListener {
                        binding.titleEtxt.text?.clear()
                        binding.descEtxt.text?.clear()
                        binding.addBtn.text = "ADD"
                        Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Data updated failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDeleteItemClick(data: Data) {
        dataCollection.document(data.id!!)
            .delete()
            .addOnSuccessListener {
                adapter.notifyDataSetChanged()
                fetchData()
                Toast.makeText(this, "Data deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Data deletion failed", Toast.LENGTH_SHORT).show()
            }
    }
}


