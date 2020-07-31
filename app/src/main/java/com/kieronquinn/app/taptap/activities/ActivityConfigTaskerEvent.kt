package com.kieronquinn.app.taptap.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionEvent
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionSatisfied


class TaskerEventHelper(config: TaskerPluginConfig<TaskerEventInput>) : TaskerPluginConfigHelper<TaskerEventInput, TaskerEventOutput, TaskerEventActionRunner>(config) {
    override val runnerClass = TaskerEventActionRunner::class.java
    override val inputClass = TaskerEventInput::class.java
    override val outputClass = TaskerEventOutput::class.java
    override fun addToStringBlurb(
        input: TaskerInput<TaskerEventInput>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append("Will trigger when the back of the phone is tapped twice")
    }
}

class ActivityConfigTaskerEvent : Activity(),TaskerPluginConfig<TaskerEventInput> {
    override fun assignFromInput(input: TaskerInput<TaskerEventInput>) {}
    override val inputForTasker get() = TaskerInput(TaskerEventInput())
    override val context get() = applicationContext
    protected val taskerHelper by lazy { TaskerEventHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper.finishForTasker()
    }
}


@TaskerInputRoot
class TaskerEventInput()

@TaskerOutputObject
class TaskerEventOutput()

@TaskerInputRoot
class TaskerEventUpdate @JvmOverloads constructor()

class TaskerEventActionRunner() : TaskerPluginRunnerConditionEvent<TaskerEventInput, TaskerEventOutput, TaskerEventUpdate>() {
    override fun getSatisfiedCondition(context: Context, input: TaskerInput<TaskerEventInput>, update: TaskerEventUpdate?): TaskerPluginResultCondition<TaskerEventOutput> {
        return TaskerPluginResultConditionSatisfied(context)
    }


}