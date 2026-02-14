import { extractBackendErrorMessage, httpRequest, httpRequestUnwrap } from './http';

export interface ApiResponse<T> {
  success: boolean;
  errorCode: string;
  data: T;
}

export interface UserProfileData {
  username: string;
  photo: string;
  id: number;
}

export const userApi = {
  async getProfile(): Promise<UserProfileData> {
    const res = await httpRequest<ApiResponse<UserProfileData>>('/user/profile', {
      method: 'GET',
    });
    if (!res.success || !res.data) {
      throw new Error(extractBackendErrorMessage(res));
    }
    return res.data;
  },

  /** 更新头像：传入图片的在线地址 URL */
  async updatePhoto(photo: string): Promise<void> {
    await httpRequestUnwrap<void>('/user/profile/photo', {
      method: 'PATCH',
      body: JSON.stringify({ photo }),
    });
  },
};
