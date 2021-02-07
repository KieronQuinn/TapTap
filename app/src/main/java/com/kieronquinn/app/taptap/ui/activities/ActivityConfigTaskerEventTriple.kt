package com.kieronquinn.app.taptap.ui.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionEvent
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionSatisfied
import com.kieronquinn.app.taptap.R


class TaskerEventHelperTriple(config: TaskerPluginConfig<TaskerEventInputTriple>) : TaskerPluginConfigHelper<TaskerEventInputTriple, TaskerEventOutputTriple, TaskerEventActionRunnerTriple>(config) {
    override val runnerClass = TaskerEventActionRunnerTriple::class.java
    override val inputClass = TaskerEventInputTriple::class.java
    override val outputClass = TaskerEventOutputTriple::class.java
    override fun addToStringBlurb(
        inpu: TaskerInput<TaskerEventInputTriple>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(config.context.getString(R.string.tasker_description_triple))
    }
}

class ActivityConfigTaskerEventTriple : Activity(),TaskerPluginConfig<TaskerEventInputTriple> {
    override fun assignFromInput(input: TaskerInput<TaskerEventInputTriple>) {}
    override val inputForTasker get() = TaskerInput(TaskerEventInputTriple())
    override val context get() = applicationContext
    protected val taskerHelper by lazy {
        TaskerEventHelperTriple(
            this
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper.finishForTasker()
    }
}


@TaskerInputRoot
class TaskerEventInputTriple()

@TaskerOutputObject
class TaskerEventOutputTriple()

@TaskerInputRoot
class TaskerEventUpdateTriple @JvmOverloads constructor()

class TaskerEventActionRunnerTriple() : TaskerPluginRunnerConditionEvent<TaskerEventInputTriple, TaskerEventOutputTriple, TaskerEventUpdateTriple>() {
    override fun getSatisfiedCondition(context: Context, input: TaskerInput<TaskerEventInputTriple>, update: TaskerEventUpdateTriple?): TaskerPluginResultCondition<TaskerEventOutputTriple> {
        return TaskerPluginResultConditionSatisfied(context)
    }


}