package activitytracker.model

import activitytracker.converter.DiffMatchPatchConverter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import name.fraser.neil.plaintext.diff_match_patch
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

public enum class DiffType {
    INSERT, DELETE
}

class Diffs @JsonCreator constructor(@JsonProperty("diffs") val diffs: List<Diff>,
                                     @JsonProperty("fileName") val fileName: String,
                                     @JsonProperty("projectName") val projectName: String,
                                     @JsonProperty("timestamp") val timestamp: Long,
                                     @JsonProperty("actionType") val action: String?,
                                     @JsonProperty("params") val params: Map<String, String>) {
    companion object {
        public fun of(old: String,
                      new: String,
                      action: String?,
                      fileName: String,
                      projectName: String,
                      params: Map<String, String>): Diffs {
            val ts = System.currentTimeMillis()
            val dmp = diff_match_patch(null)
            val diff = dmp.diff_main(old, new)
            return Diffs(DiffMatchPatchConverter.toModel(diff), fileName, projectName, ts, action, params)
        }
    }

    override fun toString(): String {
        return "Diffs {" + "\n" +
                "diffs=" + diffs + ",\n" +
                "fileName =" + fileName + ",\n" +
                "projectName =" + projectName + ",\n" +
                "timestamp =" + timestamp + "\n" +
                "}"
    }
}

class Diff @JsonCreator constructor(@JsonProperty("type") private val type: DiffType,
                                    @JsonProperty("position") private val position: Long,
                                    @JsonProperty("text") private val text: String) {

    @JsonProperty("type")
    public fun getType(): DiffType {
        return type
    }

    @JsonProperty("position")
    public fun getPosition(): Long {
        return position
    }

    @JsonProperty("text")
    public fun getText(): String {
        return text
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Diff
        return type === that.type &&
                position == that.position &&
                text == that.text

    }

    override fun toString(): String {
        return "Diff = {" + "\n" +
                "type =" + type + ",\n" +
                "position =" + position + ",\n" +
                "text =" + text + "\n" +
                "}"
    }

    override fun hashCode(): Int {
        return Objects.hash(type, position, text)
    }
}