package com.eos.parkban.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.eos.parkban.R;
import com.eos.parkban.persistence.models.ResponseResultType;

import es.dmoral.toasty.Toasty;

public class ShowToast {

    private static ShowToast instance;

    private ShowToast() {
    }

    public static ShowToast getInstance() {
        if (instance == null)
            instance = new ShowToast();
        return instance;
    }

    public void showError(Context context, int msg) {
        Toast t = null;
        if (msg == ResponseResultType.InvalidUserNameOrPassword.getValue()) {
            t = Toasty.custom(context, ResponseResultType.InvalidUserNameOrPassword.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.CanNotModify.getValue()) {
            t = Toasty.custom(context, ResponseResultType.CanNotModify.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.ErrorOnHttpGet.getValue()) {
            t = Toasty.custom(context, ResponseResultType.ErrorOnHttpGet.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.ErrorOnHttpPost.getValue()) {
            t = Toasty.custom(context, ResponseResultType.ErrorOnHttpPost.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.CanNotFindPermission.getValue()) {
            t = Toasty.custom(context, ResponseResultType.CanNotFindPermission.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.CanNotFindUser.getValue()) {
            t = Toasty.custom(context, ResponseResultType.CanNotFindUser.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.UserIsDeactive.getValue()) {
            t = Toasty.custom(context, ResponseResultType.UserIsDeactive.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.NotExistAnyData.getValue()) {
            t = Toasty.custom(context, ResponseResultType.NotExistAnyData.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.Fail.getValue()) {
            t = Toasty.custom(context, context.getResources().getString(R.string.unhandled_error_from_server), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.CanNotLoad.getValue()) {
            t = Toasty.custom(context, ResponseResultType.CanNotLoad.getDescription(), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);
        } else if (msg == ResponseResultType.InvalidToken.getValue()) {
//            showInfoMessage(context,R.string.un_authorized);
//            Intent i = new Intent(context, LoginActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //context.startActivity(i);
        } else if (msg == ResponseResultType.ExpireToken.getValue()) {
//            showInfoMessage(context,R.string.un_authorized);
//            Intent i = new Intent(context, LoginActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // context.startActivity(i);
        } else
            t = Toasty.custom(context, context.getResources().getString(msg), context.getResources().getDrawable(R.mipmap.sign), context.getResources().getColor(R.color.errorColor), Toast.LENGTH_LONG, true, true);

        t.setGravity(Gravity.TOP, 0, 130);
        t.show();
    }

    public void showSuccess(Context context, int msg) {
        Toast t = Toasty.success(context, context.getResources().getString(msg), Toast.LENGTH_LONG, true);
        t.setGravity(Gravity.TOP, 0, 130);
        t.show();
    }

    public void showWarning(Context context, int msg) {
        Toast t = Toasty.custom(context, context.getResources().getString(msg), context.getResources().getDrawable(R.mipmap.warning), context.getResources().getColor(R.color.warningColor), Toast.LENGTH_LONG, true, true);
        t.setGravity(Gravity.TOP, 0, 130);
        t.show();
    }


}
