package activitytracker.converter

import activitytracker.model.DiffType
import activitytracker.model.Diffs
import name.fraser.neil.plaintext.diff_match_patch
import name.fraser.neil.plaintext.diff_match_patch.Diff

class DiffMatchPatchConverter {
    companion object {
        public fun toModel(diff: List<Diff>): List<activitytracker.model.Diff> {
            val result = ArrayList<activitytracker.model.Diff>()
            var curInd = 0L
            for (d in diff) {
                if (d.operation == diff_match_patch.Operation.INSERT) {
                    result.add(activitytracker.model.Diff(DiffType.INSERT, curInd, d.text))
                }
                if (d.operation == diff_match_patch.Operation.DELETE) {
                    result.add(activitytracker.model.Diff(DiffType.DELETE, curInd, d.text))
                    curInd += d.text.length
                }
                if (d.operation == diff_match_patch.Operation.EQUAL) {
                    curInd += d.text.length
                }
            }
            return result
        }

        public fun fromModelOld(diffs: Diffs,
                                oldString: String): ArrayList<Diff> {
            val result = ArrayList<Diff>()
            var curInd = 0
            for (d in diffs.diffs) {
                while (d.getPosition() > curInd) {
                    result.add(Diff(diff_match_patch.Operation.EQUAL, oldString.substring(curInd, d.getPosition().toInt())))
                    curInd = d.getPosition().toInt()
                }
                if (d.getType() == DiffType.INSERT) {
                    result.add(Diff(diff_match_patch.Operation.INSERT, d.getText()))
                } else if (d.getType() == DiffType.DELETE) {
                    result.add(Diff(diff_match_patch.Operation.DELETE, d.getText()))
                    curInd += d.getText().length
                }
            }
            if (curInd < oldString.length - 1) {
                result.add(Diff(diff_match_patch.Operation.EQUAL, oldString.substring(curInd)))
            }
            return result
        }

        public fun fromModelNew(diffs: Diffs,
                                newString: String): ArrayList<Diff> {
            val result = ArrayList<Diff>()
            var curInd = 0
            var curIndNew = 0
            for (d in diffs.diffs) {
                while (d.getPosition() > curInd) {
                    val r = curIndNew + d.getPosition().toInt() - curInd
                    result.add(Diff(diff_match_patch.Operation.EQUAL, newString.substring(curIndNew, r)))
                    curIndNew = r
                    curInd = d.getPosition().toInt()
                }
                if (d.getType() == DiffType.INSERT) {
                    result.add(Diff(diff_match_patch.Operation.INSERT, d.getText()))
                    curIndNew += d.getText().length
                } else if (d.getType() == DiffType.DELETE) {
                    result.add(Diff(diff_match_patch.Operation.DELETE, d.getText()))
                    curInd += d.getText().length
                }
            }
            if (curIndNew < newString.length - 1) {
                result.add(Diff(diff_match_patch.Operation.EQUAL, newString.substring(curIndNew)))
            }
            return result
        }
    }
}