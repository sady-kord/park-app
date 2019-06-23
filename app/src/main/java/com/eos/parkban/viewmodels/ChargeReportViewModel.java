package com.eos.parkban.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.view.View;

import com.eos.parkban.BaseActivity;
import com.eos.parkban.ChargeReportActivity;
import com.eos.parkban.FunctionalityReportActivity;
import com.eos.parkban.R;
import com.eos.parkban.adapters.ChargeReportAdapter;
import com.eos.parkban.helper.DateTimeHelper;
import com.eos.parkban.helper.ShowToast;
import com.eos.parkban.persistence.ParkbanDatabase;
import com.eos.parkban.persistence.models.ResponseResultType;
import com.eos.parkban.repositories.ParkbanRepository;
import com.eos.parkban.services.dto.CashDetailsResultDto;

import java.util.Date;
import java.util.List;

public class ChargeReportViewModel extends ViewModel {

    private ChargeReportAdapter adapter = new ChargeReportAdapter();
    private ParkbanRepository parkbanRepository;
    private Date BeginDate, EndDate;
    private MutableLiveData<Boolean> showList;

    public MutableLiveData<Integer> progress = new MutableLiveData<>();
    public LiveData<Integer> getProgress() {
        return progress;
    }

    public ChargeReportAdapter getAdapter() {
        return adapter;
    }

    public LiveData<Boolean> getShowList() {
        if (showList == null)
            showList = new MutableLiveData<>();
        return showList;
    }

    public void init(final Context context){

        if (showList == null)
            showList = new MutableLiveData<>();
        showList.setValue(false);

        if (BeginDate == null)
            BeginDate = DateTimeHelper.getBeginCurrentMonth();
        if (EndDate == null)
            EndDate = DateTimeHelper.getEndCurrentMonth();

        if (parkbanRepository == null) {
            ParkbanDatabase.getInstance(context.getApplicationContext(), new ParkbanDatabase.DatabaseReadyCallback() {
                @Override
                public void onReady(ParkbanDatabase instance) {
                    parkbanRepository = new ParkbanRepository(instance);
                    if (BeginDate != null && EndDate != null) {
                        String start = DateTimeHelper.DateToString(BeginDate);
                        String end = DateTimeHelper.DateToString(EndDate);
                        getCashReport(context, start , end);
                    }
                }
            });
        }
    }

    private void getCashReport(final Context context, String startDate, String endDate){
        progress.setValue(10);

        parkbanRepository.getChargeReport(BaseActivity.CurrentUserId, startDate, endDate + " 23:59:59", new ParkbanRepository.ServiceResultCallBack<CashDetailsResultDto>() {
            @Override
            public void onSuccess(CashDetailsResultDto result) {
                progress.setValue(0);

                List<CashDetailsResultDto.CashDetailsDto> cashDetailsDtoList = result.getValue();

                if (cashDetailsDtoList.size() > 0) {
                    adapter.setCashList(cashDetailsDtoList);
                    showList.setValue(true);
                }
                else {
                    showList.setValue(false);
                    ShowToast.getInstance().showWarning(context,R.string.no_result_found);
                }
            }

            @Override
            public void onFailed(ResponseResultType resultType, String message, int errorCode) {
                progress.setValue(0);
                switch (resultType) {
                    case RetrofitError:
                        ShowToast.getInstance().showError(context, R.string.exception_msg);
                        break;
                    case ServerError:
                        if (errorCode != 0)
                            ShowToast.getInstance().showError(context, errorCode);
                        else
                            ShowToast.getInstance().showError(context, R.string.connection_failed);
                        break;
                    default:
                        ShowToast.getInstance().showError(context, resultType.ordinal());
                }
            }
        });

    }

    public void showReport(View view){

        String startDate = ((ChargeReportActivity) view.getContext()).getStartDate();
        String endDate = ((ChargeReportActivity) view.getContext()).getEndDate();

        if (endDate.compareTo(startDate) < 0){
            ShowToast.getInstance().showError(view.getContext() , R.string.begin_date_smaller_than_end_date);
            return;
        }

        getCashReport(view.getContext(),startDate, endDate);

    }
}
